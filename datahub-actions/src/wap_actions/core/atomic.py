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


class AtomicList(object):

    def __init__(self, unique_value: bool = False, cache: Cache = None):
        self._values = list()
        self._cache = cache
        self._unique_value = unique_value
        if self._cache is not None:
            c_values = self._cache.load()
            if c_values is not None and len(c_values) > 0:
                for e in c_values:
                    if e not in self._values or not self._unique_value:
                        self._values.append(e)
        self._lock = Lock()

    def add(self, element: str):
        with self._lock:
            self._add_element(element)

    def fetch_elements(self):
        cp_values = list()
        with self._lock:
            for e in self._values:
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
        if element not in self._values or not self._unique_value:
            self._values.append(element)
            if self._cache is not None:
                self._cache.add(element)
