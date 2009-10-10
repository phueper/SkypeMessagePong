
import threading
from Queue import PriorityQueue
import time

# NEWBIE QUESTION: Is this really a good idea? Does this create a new system thread?
class Timer(threading.Thread):
    def __init__(self, seconds, fun, args = {}):
        self.runTime = seconds
        self.fun = fun
        self.args = args
        threading.Thread.__init__(self)
    def run(self):
        time.sleep(self.runTime)
        self.fun(**self.args)


class Background:
    
    delayQueue = PriorityQueue()
    
    
    def runLater(self, delay, fun, args = {}):
        Timer(delay, fun, args).start()


def _test():
    def blah():
        print "hallo"
    
    b = Background()
    
    def blahblah():
        b.runLater(3, lambda: blah() )

    blahblah()