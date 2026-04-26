#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="${PROJECT_ROOT}/docker-compose.yml"
ENV_FILE="${PROJECT_ROOT}/.env"
ENV_EXAMPLE="${PROJECT_ROOT}/.env.example"
LOG_DIR="${PROJECT_ROOT}/logs"
RUN_ID="$(date +%Y%m%d_%H%M%S)"
RUN_LOG_DIR="${LOG_DIR}/run_${RUN_ID}"
LOG_POSTGRES="${RUN_LOG_DIR}/postgres.log"
LOG_API="${RUN_LOG_DIR}/api.log"

if [[ -t 1 ]]; then
  RED=$'\033[0;31m'; GREEN=$'\033[0;32m'; YELLOW=$'\033[0;33m'; BLUE=$'\033[0;34m'; MAGENTA=$'\033[0;35m'; BOLD=$'\033[1m'; RESET=$'\033[0m'
else
  RED=""; GREEN=""; YELLOW=""; BLUE=""; MAGENTA=""; BOLD=""; RESET=""
fi

sec(){ echo; echo "${BOLD}${BLUE}==> $*${RESET}"; }
info(){ echo "${BLUE}[INFO]${RESET} $*"; }
ok(){ echo "${GREEN}[OK]${RESET}   $*"; }
warn(){ echo "${YELLOW}[WARN]${RESET} $*"; }
err(){ echo "${RED}[ERR]${RESET}  $*"; }
die(){ err "$*"; exit 1; }

COMPOSE=""
LPID_POSTGRES=""
LPID_API=""

detect_compose() {
  if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
    COMPOSE="docker compose"
    return 0
  fi
  if command -v docker-compose >/dev/null 2>&1; then
    COMPOSE="docker-compose"
    return 0
  fi
  return 1
}

check_prereqs() {
  command -v docker >/dev/null 2>&1 || die "Docker non installé."
  docker info >/dev/null 2>&1 || die "Docker daemon indisponible. Lance Docker Desktop / service docker."
  detect_compose || die "Docker Compose introuvable."
}

ensure_env() {
  if [[ -f "$ENV_FILE" ]]; then
    ok ".env détecté"
    return 0
  fi

  if [[ -f "$ENV_EXAMPLE" ]]; then
    cp "$ENV_EXAMPLE" "$ENV_FILE"
    ok ".env créé depuis .env.example"
  else
    die "Aucun .env ni .env.example trouvé."
  fi
}

init_logs() {
  mkdir -p "$RUN_LOG_DIR"
  : > "$LOG_POSTGRES"
  : > "$LOG_API"
}

prefix_stream() {
  local color="$1"
  local tag="$2"
  local logfile="$3"

  awk -v color="$color" -v reset="$RESET" -v tag="$tag" \
    '{ print color "[" tag "]" reset " " $0; fflush(); }' | tee -a "$logfile"
}

wait_for_api() {
  python3 - <<'PY'
import time, urllib.request, sys

for _ in range(60):
    try:
        with urllib.request.urlopen('http://localhost:8000/health', timeout=2) as r:
            print('API healthy:', r.status)
            sys.exit(0)
    except Exception:
        time.sleep(1)

print('API healthcheck timeout')
sys.exit(1)
PY
}

start_services() {
  sec "Build et démarrage des conteneurs"
  $COMPOSE -f "$COMPOSE_FILE" up --build -d
  ok "Conteneurs lancés"
}

status_services() {
  $COMPOSE -f "$COMPOSE_FILE" ps
}

down_services() {
  $COMPOSE -f "$COMPOSE_FILE" down
}

reset_services() {
  $COMPOSE -f "$COMPOSE_FILE" down -v
}

show_runtime_info() {
  info "Swagger: http://localhost:8000/docs"
  info "Compte admin de seed éventuel : voir .env ou le script de seed"
  warn "Ne pas afficher l'email et le mot de passe admin en clair dans le terminal."
}

ask_log_display() {
  if [[ ! -t 0 ]]; then
    info "Mode non interactif détecté : les logs ne seront pas affichés dans le terminal."
    return 1
  fi

  while true; do
    read -r -p "Voulez-vous afficher les logs dans le terminal ? (oui/non) : " answer
    case "${answer,,}" in
      o|oui|y|yes)
        return 0
        ;;
      n|non|no)
        return 1
        ;;
      *)
        warn "Réponse invalide. Tapez oui ou non."
        ;;
    esac
  done
}

stop_log_followers() {
  warn "Arrêt du suivi des logs"
  [[ -n "${LPID_POSTGRES:-}" ]] && kill "$LPID_POSTGRES" >/dev/null 2>&1 || true
  [[ -n "${LPID_API:-}" ]] && kill "$LPID_API" >/dev/null 2>&1 || true
  LPID_POSTGRES=""
  LPID_API=""
  trap - INT TERM
}

interrupt_menu() {
  echo
  warn "Interruption détectée."
  echo "1) Continuer"
  echo "2) Quitter l'affichage des logs"
  echo "3) Stopper les services"
  echo "4) Reset complet"

  while true; do
    read -r -p "Votre choix [1-4] : " choice
    case "$choice" in
      1)
        info "Reprise de l'affichage des logs"
        return 0
        ;;
      2)
        stop_log_followers
        ok "Affichage des logs arrêté. Les services continuent de tourner."
        exit 0
        ;;
      3)
        stop_log_followers
        down_services
        ok "Services arrêtés."
        exit 0
        ;;
      4)
        stop_log_followers
        reset_services
        ok "Reset terminé."
        exit 0
        ;;
      *)
        warn "Choix invalide. Entrez 1, 2, 3 ou 4."
        ;;
    esac
  done
}

follow_logs() {
  sec "Logs temps réel"
  info "Les logs complets sont enregistrés dans ${RUN_LOG_DIR}"

  $COMPOSE -f "$COMPOSE_FILE" logs -f postgres 2>&1 | prefix_stream "$MAGENTA" "postgres" "$LOG_POSTGRES" &
  LPID_POSTGRES=$!

  $COMPOSE -f "$COMPOSE_FILE" logs -f api 2>&1 | prefix_stream "$GREEN" "api" "$LOG_API" &
  LPID_API=$!

  trap interrupt_menu INT TERM

  while kill -0 "$LPID_POSTGRES" >/dev/null 2>&1 || kill -0 "$LPID_API" >/dev/null 2>&1; do
    sleep 1
  done

  stop_log_followers
}

cmd_start() {
  init_logs
  start_services

  sec "Vérification API"
  wait_for_api && ok "API disponible sur http://localhost:8000" || die "API non disponible après démarrage"

  show_runtime_info

  if ask_log_display; then
    follow_logs
  else
    ok "Services démarrés sans affichage des logs"
    info "Pour afficher les logs plus tard : ./run.sh logs"
  fi
}

cmd_logs() {
  init_logs
  follow_logs
}

cmd_status() {
  status_services
}

cmd_down() {
  down_services
}

cmd_reset() {
  reset_services
}

show_usage() {
  echo "Usage: ./run.sh {start|logs|status|down|reset}"
}

dispatch_command() {
  local cmd="${1:-start}"

  case "$cmd" in
    start)
      cmd_start
      ;;
    logs)
      cmd_logs
      ;;
    status)
      cmd_status
      ;;
    down|stop)
      cmd_down
      ;;
    reset)
      cmd_reset
      ;;
    *)
      show_usage
      exit 1
      ;;
  esac
}

check_prereqs
ensure_env
dispatch_command "${1:-start}"