from typing import Optional
from pydantic import BaseModel
from wap_actions.service.ci_token import CITokenConfig


class CallbackApi(BaseModel):
    region: str
    url: str


class UrlNotificationConfig(BaseModel):
    type: str
    output_json: Optional[bool]
    cache_root: str
    entity_types: Optional[list[str]]
    entity_props: Optional[list[str]]
    ci_config: Optional[CITokenConfig]
    callbacks: list[CallbackApi]
