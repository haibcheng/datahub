import json
import logging
from typing import Optional

from pydantic import BaseModel

from datahub_actions.action.action import Action
from datahub_actions.event.event_envelope import EventEnvelope
from datahub_actions.pipeline.pipeline_context import PipelineContext

logger = logging.getLogger(__name__)


class DatasourceMonitorConfig(BaseModel):
    # Whether to print the message in upper case.
    to_upper: Optional[bool]


class DatasourceMonitorAction(Action):
    @classmethod
    def create(cls, config_dict: dict, ctx: PipelineContext) -> "Action":
        action_config = DatasourceMonitorConfig.parse_obj(config_dict or {})
        return cls(action_config, ctx)

    def __init__(self, config: DatasourceMonitorConfig, ctx: PipelineContext):
        self.config = config

    def act(self, event: EventEnvelope) -> None:
        logger.info("Hello datasource! Received event:")
        message = json.dumps(json.loads(event.as_json()), indent=4)
        if self.config.to_upper:
            logger.info(message.upper())
        else:
            logger.info(message)

    def close(self) -> None:
        pass
