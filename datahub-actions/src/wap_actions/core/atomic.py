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
        self._value = list()
        self._cache = cache
        self._unique_value = unique_value
        if self._cache is not None:
            c_values = self._cache.load()
            if c_values is not None and len(c_values) > 0:
                for e in c_values:
                    if e not in self._value or not self._unique_value:
                        self._value.append(e)
        self._lock = Lock()

    def add(self, element):
        with self._lock:
            if element not in self._value or not self._unique_value:
                self._value.append(element)
                if self._cache is not None:
                    self._cache.add(element)

    def fetch_elements(self):
        with self._lock:
            cp_values = self._value.copy()
            self._value.clear()
            if self._cache is not None:
                for e in cp_values:
                    self._cache.remove(e)
            return cp_values

    def add_elements(self, elements):
        if elements is None or len(elements) == 0:
            return
        with self._lock:
            for e in elements:
                self.add(e)
