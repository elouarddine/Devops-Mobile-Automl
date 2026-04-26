from typing import Any, Generic, Optional, Type, TypeVar

T = TypeVar("T")


class SQLAlchemyBaseRepository(Generic[T]):
    def __init__(self, session, model: Type[T]):
        self.session = session
        self.model = model

    def save(self, entity: T) -> T:
        try:
            self.session.add(entity)
            self.session.commit()
            self.session.refresh(entity)
            return entity
        except Exception:
            self.session.rollback()
            raise

    def update(self, entity: T) -> T:
        try:
            self.session.commit()
            self.session.refresh(entity)
            return entity
        except Exception:
            self.session.rollback()
            raise

    def find_by_id(self, entity_id: int) -> Optional[T]:
        return self.session.query(self.model).filter_by(id=entity_id).first()

    def find_one_by(self, **filters: Any) -> Optional[T]:
        return self.session.query(self.model).filter_by(**filters).first()

    def find_many_by(self, **filters: Any) -> list[T]:
        return self.session.query(self.model).filter_by(**filters).all()


    def delete(self, entity: T) -> None:
        try:
            self.session.delete(entity)
            self.session.commit()
        except Exception:
            self.session.rollback()
            raise
