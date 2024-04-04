import paho.mqtt.client as mqtt
import random
import time
import logging
from datetime import datetime, timedelta
from Crypto.Cipher import AES
from Crypto.Util.Padding import pad
import os

broker_address = "localhost"
broker_port = 1883
topic = "transport/data"
vehicle_data = [{'imei': '605486034236830', 'latitude': '39.756837', 'longitude': '28.887858'},
                {'imei': '605486034236831', 'latitude': '39.756837', 'longitude': '28.887858'},
                {'imei': '605486034236832', 'latitude': '39.756837', 'longitude': '28.887858'},
                {'imei': '605486034236833', 'latitude': '39.756837', 'longitude': '28.887858'},
                {'imei': '605486034236834', 'latitude': '39.756837', 'longitude': '28.887858'},
                {'imei': '605486034236835', 'latitude': '39.756837', 'longitude': '28.887858'},
                {'imei': '605486034236836', 'latitude': '39.756837', 'longitude': '28.887858'},
                {'imei': '605486034236837', 'latitude': '39.756837', 'longitude': '28.887858'},
                {'imei': '605486034236838', 'latitude': '39.756837', 'longitude': '28.887858'},
                {'imei': '605486034236839', 'latitude': '39.756837', 'longitude': '28.887858'}]

# Logging configuration
logger = logging.getLogger('simulator_logger')
logger.setLevel(logging.INFO)
file_handler = logging.FileHandler('simulator.log')
file_handler.setLevel(logging.INFO)
formatter = logging.Formatter('%(asctime)s - %(levelname)s - %(message)s')
file_handler.setFormatter(formatter)
logger.addHandler(file_handler)

# AES encryption setup
def encrypt_data(data, key):
    cipher = AES.new(key, AES.MODE_CBC)
    ct_bytes = cipher.encrypt(pad(data.encode(), AES.block_size))
    iv = cipher.iv
    return iv + ct_bytes

# MQTT setup
client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION1)

def on_connect(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))
    client.subscribe(topic)

def on_disconnect(client, userdata, rc):
    print("Disconnected with result code "+str(rc))
    while True:
        try:
            print("Attempting to reconnect...")
            client.reconnect()
            print("Reconnected successfully!")
            break
        except:
            print("Reconnection failed. Retrying in 5 seconds...")
            time.sleep(0)

client.on_connect = on_connect
client.on_disconnect = on_disconnect
client.connect(broker_address, broker_port, 60)
client.loop_start()
def calculate_checksum(header):
    checksum = 0
    for char in header:
        checksum += ord(char)
    return checksum
# Generate ECU data packet
def generate_data_packet(imei, latitude, longitude, current_time=datetime.now(), frame_number='0000000'):
    packet_types = ['NR', 'OBD', 'DTC']
    packet_status_types = ['L','H']
    start_char = '$'
    header = 'Header'
    firmware_version = '1.0'
    config_version = '3.5'
    packet_type = random.choice(packet_types)
    packet_status = random.choice(packet_status_types)
    gps_fix = '1' if random.choice([True, False]) else '0'
    date = datetime.now().strftime('%d%m%Y')
    time = current_time.strftime('%H%M%S')
    time += (current_time + timedelta(seconds=10)).strftime('%S')
    latitude_direction = random.choice(['N', 'S'])
    longitude_direction = random.choice(['E', 'W'])
    speed = '{:.1f}'.format(random.uniform(0, 200))
    heading = str(random.randint(0, 360))
    num_satellites = str(random.randint(0, 12))
    altitude = str(random.randint(0, 5000))
    pdop = '{:.1f}'.format(random.uniform(0, 10))
    hdop = '{:.1f}'.format(random.uniform(0, 10))
    network_operator_name = 'Jio'
    ignition_status = '1'
    main_input_voltage = '{:.1f}'.format(random.uniform(10, 14))
    gsm_signal_strength = str(random.randint(0, 100))
    gprs_status = '1'
    end_char = '*'
    
    # Concatenate all parameters except start and end characters
    data_to_checksum = ','.join([header, firmware_version, config_version, packet_type, packet_status,
                                 imei, gps_fix, date, time, latitude, latitude_direction, longitude,
                                 longitude_direction, speed, heading, num_satellites, altitude, pdop, hdop,
                                 network_operator_name, ignition_status, main_input_voltage, gsm_signal_strength,
                                 gprs_status, frame_number])
    print("dnjaskndas",data_to_checksum)

    #for loop datatochecksum
    #if interher or float numofstatellites,spped,lattitude 
    #addcecksum
    #else
    #call string checksumfunction
    elemnets = data_to_checksum.split(',')
    added_chksum= 0
    for elemnets in elemnets:
        added_chksum+=calculate_checksum(elemnets)
    checksum = str(added_chksum)
    print(added_chksum,"added_chksum")
    # Construct the data packet
    data_packet = ','.join([start_char, header, firmware_version, config_version, packet_type, packet_status,
                            imei, gps_fix, date, time, latitude, latitude_direction, longitude,
                            longitude_direction, speed, heading, num_satellites, altitude, pdop, hdop,
                            network_operator_name, ignition_status, main_input_voltage, gsm_signal_strength,
                            gprs_status, frame_number, end_char + checksum])
    print(f"DATA {data_packet}")


    
    

    return data_packet

try:
    i = 0
    while True:
        frame_number = '{:07d}'.format(i + 1)
        for vehicle in vehicle_data:
            data_packet = generate_data_packet(vehicle['imei'], vehicle['latitude'], vehicle['longitude'], frame_number=frame_number)
            encrypted_data = encrypt_data(data_packet, b'Sixteen byte key')
            client.publish(topic, encrypted_data)
            logger.info(data_packet)
        i += 1
        time.sleep(50000)  # 0Adjust the frequency of data transmission as needed
except KeyboardInterrupt:
    pass

# Disconnect from the broker
client.disconnect()