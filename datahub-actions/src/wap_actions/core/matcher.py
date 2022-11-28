import json
from wap_actions.core.configuration import UrlNotificationConfig


class Matcher:

    def matches(self, event_json):
        return True


class EntityTypeMatcher(Matcher):

    def matches(self, event_json):
        if self.match_type != self.config.type:
            return False
        if self.config.entity_types is not None and len(self.config.entity_types) > 0:
            entity_type = event_json.get("entityType")
            if entity_type is None or entity_type not in self.config.entity_types:
                return False
        return True

    def __init__(self, config: UrlNotificationConfig, match_type: str):
        self.config = config
        self.match_type = match_type


class PropValueMatcher(EntityTypeMatcher):

    def matches(self, event_json):
        if not super().matches(event_json):
            return False
        if self.config.entity_props is not None and len(self.config.entity_props) > 0:
            if self.aspect_name != event_json.get("aspectName"):
                return False
            aspect = event_json.get("aspect")
            if aspect is None or "value" not in aspect.keys():
                return False
            props = json.loads(aspect.get("value").replace("\\", "")).get("customProperties")
            return self._match_value(props)
        return True

    def _match_value(self, props):
        for each_prop in self.config.entity_props:
            pos = each_prop.index("=")
            key = each_prop[0:pos].strip()
            if key not in props.keys():
                return False
            if props[key] != each_prop[pos+1:len(each_prop)].strip():
                return False
        return True

    def __init__(self, config: UrlNotificationConfig, match_type: str, aspect_name: str):
        super().__init__(config=config, match_type=match_type)
        self.aspect_name = aspect_name
