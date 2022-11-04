from threading import Lock


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
