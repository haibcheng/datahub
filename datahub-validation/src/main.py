import os
from jobs import OnceJob
from constant import Constant

if __name__ == '__main__':
    job_type = os.getenv(Constant.job_type)
    if job_type == 'once':
        job = OnceJob()
        job.sync()
