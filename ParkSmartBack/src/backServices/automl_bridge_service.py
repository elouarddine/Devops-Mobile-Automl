from pathlib import Path


class AutoMLBridgeService:
    """Point d'entrée léger pour brancher ton vrai dossier AutoML."""

    def __init__(self, automl_root: str | None = None):
        self.automl_root = Path(automl_root) if automl_root else None

    def train(self, dataset_path: str | None = None) -> dict:
        return {
            "mode": "train",
            "metrics": {"rmse": 4.1, "mae": 2.3},
            "charts": {"loss": [{"x": 1, "y": 0.8}, {"x": 2, "y": 0.4}]},
            "summary": f"Hook AutoML prêt. Dataset utilisé: {dataset_path or 'N/A'}",
        }

    def evaluate(self, dataset_path: str | None = None) -> dict:
        return {
            "mode": "eval",
            "metrics": {"rmse": 3.8, "mae": 2.1},
            "charts": {"pred_vs_true": [{"x": 1, "y": 1.2}, {"x": 2, "y": 1.9}]},
            "summary": f"Évaluation simulée prête à être remplacée par ton vrai AutoML. Dataset: {dataset_path or 'N/A'}",
        }
