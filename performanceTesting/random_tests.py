#!/usr/bin/python3

import random, string
import requests
import threading
import argparse, subprocess, os
import inspect

PROXY = "http://dh2020pc05:8026/"
filename = inspect.getframeinfo(inspect.currentframe()).filename
CWD     = os.path.dirname(os.path.abspath(filename))


class testThread(threading.Thread):
	def __init__(self, thread_id, read_tests, write_tests):
		threading.Thread.__init__(self)
		self.thread_id = thread_id
		self.read_tests = read_tests
		self.write_tests = write_tests
	
	def run(self):
		GETResponse = send_GET_Request(self.read_tests)
		# for i in range (10):
		# 	subprocess.run(["./stopNode"], cwd=f"{CWD}/../scripts")
		# 	getres = send_GET_Request(self.read_tests)
		# 	print(f"{i},{getres}")
		# print("read response from thread" + str(self.thread_id) + ": " + str(GETResponse))
		PUTResponse = send_PUT_Request(self.write_tests)
		# print("write response from thread" + str(self.thread_id) + ": " + str(PUTResponse))

def get_random_string(length):
	letters = string.ascii_lowercase
	result_str = ''.join(random.choice(letters) for i in range(length))
	return result_str

# def send_GET_Request(num_tests):
# 	response = {"success": 0, "fail": 0}
# 	tic = None
# 	for i in range(num_tests):
# 		try:
# 			request = requests.get(PROXY + get_random_string(8))
# 			if request.status_code == 404:
# 				# if i % 60 == 0:
# 				# 	print("1")
# 				if not tic is None:
# 					print("timer ended")
# 					toc = time.perf_counter()
# 					print(f"{toc-tic:0.6f}")
# 					break
# 				response["success"] += 1

# 			else:
# 				response["fail"] += 1
# 		except:
# 			if tic is None:
# 				print("timer started")
# 				tic = time.perf_counter()
# 				num_tests = num_tests - 2
# 	return response

def send_GET_Request(num_tests):
	response = {"success": 0, "fail": 0}
	tic = None
	for i in range(num_tests):
		try:
			request = requests.get(PROXY + get_random_string(8))
			if request.status_code == 404:
				if not tic is None:
					toc = time.perf_counter()
					print(f"{toc-tic:0.6f}")
					break
				response["success"] += 1

			else:
				response["fail"] += 1
		except:
			if tic is None:
				tic = time.perf_counter()
				num_tests = num_tests - 2
	return response

def send_PUT_Request(num_tests):
	response = {"success": 0, "fail": 0}

	for i in range(num_tests):
		longResource = "http://" + get_random_string(20)
		shortResource = get_random_string(8)
		try:
			request = requests.put(PROXY + "?short=" + shortResource + "&long=" + longResource)
			if request.status_code == 200:
				response["success"] += 1
			else:
				response["fail"] += 1
		except:
			print("Error sending request")
	return response

def test(num_threads, total_read_tests, total_write_tests):
	threads = []
	read_tests_per_thread = total_read_tests // num_threads
	write_tests_per_thread = total_write_tests // num_threads
	for thread_id in range(num_threads):
		threads.append(testThread(thread_id, read_tests_per_thread, write_tests_per_thread))
	for t in threads:
		t.start()
	for t in threads:
		t.join()


if __name__=='__main__':
	import time
	import sys
	
	if len(sys.argv) != 6:
		print("Usage: python3 random_tests.py [proxy_host] [hostname] [number_of_threads] [number_of_read_tests] [number_of_write_tests]")
		sys.exit(1)
	
	proxy_host = sys.argv[1]
	PROXY = "http://" + proxy_host + ":8026/"
	hostname = sys.argv[2]
	num_threads = int(sys.argv[3])
	read_tests = int(sys.argv[4])
	write_tests = int(sys.argv[5])


	tic = time.perf_counter()
	test(num_threads, read_tests, write_tests)
	toc = time.perf_counter()
	# print(f"{hostname:s} {num_threads:d} {read_tests:d} {write_tests:d} {toc-tic:0.6f}")
	# print(f"{read_tests:d},{write_tests:d},{toc-tic:0.6f}")
