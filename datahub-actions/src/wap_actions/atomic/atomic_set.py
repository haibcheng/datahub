from threading import Lock


class AtomicSet(object):

    def __init__(self):
        self._value = set()
        self._lock = Lock()

    def add(self, element):
        with self._lock:
            self._value.add(element)
            return self._value

    def copy_of(self):
        with self._lock:
            n_set = self._value.copy()
            self._value.clear()
            return n_set

    def add_set(self, elements):
        with self._lock:
            for e in elements:
                self._value.add(e)
