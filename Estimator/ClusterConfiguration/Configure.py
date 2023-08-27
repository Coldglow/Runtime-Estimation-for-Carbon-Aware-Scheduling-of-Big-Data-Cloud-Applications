import collections
import logging
import sys

import pandas as pd
import numpy as np
import scipy
from sklearn.model_selection import train_test_split

from RuntimePrediction.Predict import Predictor


### CONFIG ###
scaleout_range = 2, 14, 2 # Min, max, step, change the number of nodes that want to be estimated
confidence = 0.95

Model = collections.namedtuple('Model', ['name', 'predictor', 'kwargs'])
Job = collections.namedtuple('Job', ['name', 'X', 'y'])

kmeans_df = pd.read_csv('C:/code/estimator/data/KMeans.csv', sep=',')


def get_training_data(df, features, filters):
    # Get medians
    g = df.groupby(by=['instance_count','machine_type'] + features)
    df = pd.DataFrame(g.median().to_records())
    for k, s, v in filters:
        if s == '==': df = df[df[k] == v]
        if s == '>' : df = df[df[k] >= v]
    X = df[['instance_count'] + features]
    y = (df[['gross_runtime']]).squeeze()
    return X, y


# def get_training_data(df, features):
#     # Get medians
#     g = df.groupby(by=['instance_count','machine_type'] + features)
#     df = pd.DataFrame(g.median().to_records())
#     X = df[['instance_count'] + features]
#     y = (df[['gross_runtime']]).squeeze()
#     return X, y


def get_machine_type(job_name):
    mtypes = {'Sort': 'n2d-standard-2',
              'K-Means': 'n2d-standard-2'}
    return mtypes[job_name]


def get_jobs():
    jobs = [Job('K-Means',
            *(get_training_data(kmeans_df,
                 ['observations', 'features', 'k'],
                 [('machine_type', '==', 'n2d-standard-2'),
                  ('instance_count', '>=', 2)])))]
    # df, features, filters
    return jobs


def get_configuration(job_name, *args):

    jobs = {job.name: job for job in get_jobs()}

    # Verify the input
    if not job_name in jobs:
        logging.error(f"Job '{job_name}' has no runtime data available.")
        exit(1)

    # print(f"Configuring cluster to execute a {job_name} job in {max_runtime}s" +\
    #       f" with a confidence of {confidence}")

    job = jobs[job_name]
    # keys = ', '.join(k for k in job.X.keys()[1:])
    if not len(job.X.keys()) == len(args)+1:
        # print(f"Job '{job.name}' requires {len(job.X.keys())-1} context args:" +\
        #       f" {keys} but {'none' if len(args)==0 else len(args)} were given")
        exit(1)

    # values = '\n'.join(f"    {k}: {v}" for k,v in zip(job.X.keys()[1:], args))
    # logging.info(f"Execution context for {job.name}:\n{values}")
    # print(f"Execution context for {job.name}:\n{values}")

    # Estimate the accuracy of the runtime predictor
    rtpred = Predictor()
    X_tr, X_te, y_tr, y_te = train_test_split(job.X, job.y, test_size=0.1)
    rtpred.fit(X_tr, y_tr)
    # y_hat = rtpred.predict(X_te)
    # errors = (y_hat - y_te).to_numpy()
    # mu, sigma = errors.mean(), errors.std()
    # print(f"Estimated mean runtime prediction error: {mu:.2f}s, " +\
    #       f"standard deviation: {sigma:.2f}s")

    # x = scipy.special.erfinv(2 * confidence - 1) * np.sqrt(2)

    # tolerance = mu + x * sigma
    # print(f"Required tolerance to reach the deadline in {confidence*100}%"+\
    #       f" of cases: {tolerance:.2f}s")

    rtpred.fit(job.X, job.y)
    possibilities = np.array(list((so, *args) for so in range(*scaleout_range)))
    y_hat = rtpred.predict(possibilities)

    tmp = kmeans_df.groupby('instance_count')['cpu_usage'].mean()
    cpu_usage = [v for k, v in tmp.items()]
    runtime = []
    for so, rt in zip(range(*scaleout_range), y_hat):
        runtime.append(rt)
        # if rt+tolerance <= max_runtime:
        #     if rt < resulting_runtime:
        #         chosen_scaleout, resulting_runtime = so, rt

    for i in range(len(cpu_usage)):
        print(round(runtime[i]), '%.2f' % cpu_usage[i])



