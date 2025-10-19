from pydantic import BaseModel


class MetaDadosDTO(BaseModel):
    """
    DTO para transferir dados básicos como Idade, Sexo e Localização da lesão.
    """
    idade: int
    sexo: str
    localizacao: str

