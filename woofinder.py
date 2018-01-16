# -*- coding: utf-8 -*-

import RPi.GPIO as GPIO
import socket
import threading#!/bin/bash 
import os
import urllib.request
import time

LED = 21
host = "192.168.0.122"
GPIO.setmode(GPIO.BCM)

def sound_play(file):
    os.system("omxplayer -o local ./"+file)
    
def TTL_request(msg):
    client_id = ""           # <= 변경
    client_secret = "" # <= 변경
    encText = urllib.parse.quote(msg)
    data = "speaker=mijin&speed=0&text=" + encText;
    url = "https://openapi.naver.com/v1/voice/tts.bin"
    request = urllib.request.Request(url)
    request.add_header("X-Naver-Client-Id",client_id)
    request.add_header("X-Naver-Client-Secret",client_secret)
    response = urllib.request.urlopen(request, data=data.encode('utf-8'))
    rescode = response.getcode()
    if(rescode==200):
        print("TTS mp3 저장")
        response_body = response.read()
        with open('speech.mp3', 'wb') as f:
            f.write(response_body)
    else:
        print("Error Code:" + rescode)
        
def led_control():
    GPIO.setup(LED,GPIO.OUT)
    clientSock = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
    clientSock.bind(("",111))

    try:
        while True:
            data,svr_addr = clientSock.recvfrom(1024)
            print(str(data.strip()),svr_addr)
            if str(data.strip())=="b'on'":
                GPIO.output(LED,GPIO.HIGH)
            elif str(data.strip())=="b'off'":
                GPIO.output(LED,GPIO.LOW)    
            if str(data.strip())=="b'sound'":
                sound_play("sitdown.wav")
                print("sit down!")
            if "@TTL##" in str(data.strip()):
                msg = str(data.strip(),"utf8")
                TTL_request(msg[6:])
                sound_play("speech.mp3")
                time.sleep(1)
            clientSock.sendto(("complete!").encode(),svr_addr)
            
    except KeyboardInterrupt:
        GPIO.cleanup()
def CAM_stream_start():
    os.system("raspivid -t 0 -h 720 -w 1280 -fps 25 -hf -vf -b 2000000 -o - | gst-launch-1.0 -v fdsrc ! h264parse ! rtph264pay config-interval=1 pt=96 ! gdppay ! tcpserversink host="+host+" port=5000")

class LED_thread(threading.Thread):
    def __init__(self, threadID, name):
        threading.Thread.__init__(self)
        self.threadID = threadID
        self.name = name
    def run(self):
        led_control()


class Cam_thread(threading.Thread):
    def __init__(self, threadID, name):
        threading.Thread.__init__(self)
        self.threadID = threadID
        self.name = name
    def run(self):
        CAM_stream_start()

if __name__=="__main__":
    led_t = LED_thread(1,"LED_thread")
    cam_t = Cam_thread(2,"CAM_thread")
    led_t.start()
    cam_t.start()
