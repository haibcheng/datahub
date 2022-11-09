import logging

logger = logging.getLogger(__name__)


class Cache:

    def __init__(self):
        self._contents = list()

    def load(self):
        return self._contents

    def add(self, e):
        if e is not None:
            self._contents.append(e)

    def remove(self, e):
        if e is not None:
            self._contents.remove(e)

    def persist(self):
        pass


class FileCache(Cache):

    def __init__(self, pa: str, fn: str):
        super().__init__()
        if pa is not None and pa != '':
            self.fi = pa + '/' + fn
        else:
            self.fi = fn

    def load(self):
        try:
            with open(self.fi, mode='r', encoding='utf-8') as f:
                lines = f.readlines()
                for line in lines:
                    xline = line.strip()
                    if xline != '':
                        self.add(xline)
                        logger.info("Loading %s...", xline)
        except IOError:
            pass
        return super().load()

    def persist(self):
        with open(self.fi, mode='w', encoding='utf-8') as f:
            f.writelines(self._contents)
