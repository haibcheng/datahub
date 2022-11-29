from threading import Lock
from wap_actions.core.cache import Cache


class AtomicInteger(object):

    def __init__(self):
        self._value = 0
        self._lock = Lock()

    def increment_get(self):
        with self._lock:
            self._value += 1
            return self._value

    def reset(self):
        with self._lock:
            self._value = 0


class AtomicDict(object):

    def __init__(self, unique_value: bool = False, cache: Cache = None, separator: str = ""):
        self._values = dict()
        self._cache = cache
        self._unique_value = unique_value
        self._separator = separator
        if self._cache is not None:
            c_values = self._cache.load()
            if c_values is not None and len(c_values) > 0:
                for e in c_values:
                    key = self.key_of(e)
                    if key not in self._values.keys() or not self._unique_value:
                        self._values[key] = self.value_of(e)
        self._lock = Lock()

    def key_of(self, element: str):
        if self._separator is None or self._separator == "":
            return element
        try:
            idx = element.index(self._separator)
            return element[:idx]
        except ValueError:
            return element

    def value_of(self, element: str):
        if self._separator is None or self._separator == "":
            return ""
        try:
            idx = element.index(self._separator)
            return element[idx + len(self._separator):]
        except ValueError:
            return ""

    def add(self, key: str, value: str = None):
        with self._lock:
            if value is None:
                self._add_element(key)
            else:
                self._add_element(key + self._separator + value)

    def fetch_elements(self):
        cp_values = list()
        with self._lock:
            for e in self._values.keys():
                if self._values[e] != "":
                    e += self._separator + self._values[e]
                cp_values.append(e)
                if self._cache is not None:
                    self._cache.remove(e)
            self._values.clear()
        return cp_values

    def add_elements(self, elements: list[str]):
        if elements is None or len(elements) == 0:
            return
        with self._lock:
            for e in elements:
                self._add_element(e)

    def _add_element(self, element: str):
        key = self.key_of(element)
        if key not in self._values.keys() or not self._unique_value:
            self._values[key] = self.value_of(element)
            if self._cache is not None:
                self._cache.add(element)
