#!/usr/bin/python3

import random, string
import requests
import threading

PROXY = "http://dh2020pc05:8030/"

SHORT_POOLS = []

class testThread(threading.Thread):
	def __init__(self, thread_id, read_tests):
		threading.Thread.__init__(self)
		self.thread_id = thread_id
		self.read_tests = read_tests
		self.short_pool = SHORT_POOLS[self.thread_id]

	def run(self):
		GETResponse = send_GET_Request(self.short_pool, self.read_tests)

def get_random_string(length):
	letters = string.ascii_lowercase
	result_str = ''.join(random.choice(letters) for i in range(length))
	return result_str

def send_GET_Request(str_pool, num_tests):
	response = {"success": 0, "fail": 0}
	for i in range(num_tests):
		try:
			request = requests.get(PROXY + str_pool[i])
			if request.status_code == 404:
				response["success"] += 1
			else:
				response["fail"] += 1
		except:
			print("Error sending request " + str_pool[i])
	return response

def get_string_arr(str_len, arr_size):
	str_pool = []
	for _ in range(arr_size):
		str_pool.append(get_random_string(str_len))
	return str_pool

def populate_short_pools(str_len, num_arrs, strs_per_arr):
	for _ in range(num_arrs):
		SHORT_POOLS.append(get_string_arr(str_len, strs_per_arr))

def test(num_threads, read_tests_per_thread):
	threads = []
	for thread_id in range(num_threads):
		threads.append(testThread(thread_id, read_tests_per_thread))
	for t in threads:
		t.start()
	for t in threads:
		t.join()

if __name__=='__main__':
	import time
	import sys
	
	if len(sys.argv) != 5:
		print("Usage: python3 cache_tests.py [proxy_host] [number_of_threads] [hostname] [number_of_read_tests]")
		sys.exit(1)
	
	proxy_host = sys.argv[1]
	PROXY = "http://" + proxy_host + ":8030/"
	
	hostname = sys.argv[2]
	num_threads = int(sys.argv[3])
	read_tests = int(sys.argv[4])

	read_tests_per_thread = read_tests // num_threads

	short_len = 8
	populate_short_pools(short_len, num_threads, read_tests_per_thread)

	for i in range(3):
		tic = time.perf_counter()
		test(num_threads, read_tests_per_thread)
		toc = time.perf_counter()
		print(f"{hostname:s} {num_threads:d} {i:d} {read_tests:d} {toc-tic:0.6f}")
