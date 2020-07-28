from numpy import *
import cv2

def resize(numlist):
    # numlist = reshape(numlist,(3,3))
    numlist = array(numlist).astype(float)
    numlist = cv2.resize(numlist,(56,56))
    result = numlist.tolist()
    return result