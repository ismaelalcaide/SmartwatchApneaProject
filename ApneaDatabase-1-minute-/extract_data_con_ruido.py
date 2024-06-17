import numpy as np 
import pyedflib
import os
import csv
import soundfile as sf

from annotate import annotate

# generate random numbers from a Gaussian distribution with the given standard deviation  
def gaussian_white_noise(signal, stddev):
    noise = np.random.normal(1, stddev, len(signal)) 
    noisy_signal = signal * noise 
    return noisy_signal

# extract edf file content
def edf_to_csv(file, timestamp_rate = 60, stddev = 0, fragment_n = 0):

    f = pyedflib.EdfReader(file)

    temp = gaussian_white_noise(f.readSignal(11)[5::timestamp_rate*100], stddev)
    
    spo2 = gaussian_white_noise(f.readSignal(15)[5::timestamp_rate], stddev)
    body = gaussian_white_noise(f.readSignal(16)[5::timestamp_rate], stddev)
    #movimiento = np.select([body < 1.5, body < 4.5], ['Up', 'Right'], 'Left')
    movimiento = np.select([body < 1.5, body < 4.5], ['3', '1'], '2')
    hr = gaussian_white_noise(f.readSignal(17)[5::timestamp_rate], stddev)

    duration = len(hr)

    timestamp = np.arange(duration*timestamp_rate*fragment_n, duration*timestamp_rate*(fragment_n+1), 60)

    sound = gaussian_white_noise(f.readSignal(19), stddev)

    return np.array([timestamp, hr, spo2, movimiento, temp[:duration]]).T, sound

timestamp_rate = 60
stddev = 0.05
save_audio = False


# Create Data directory and get all folders (patients) and edf files
if not os.path.exists("APNEA_DATA"):
    os.makedirs("APNEA_DATA")

base_path = "APNEA_EDF"
folders = [folder for folder in os.listdir(base_path) if os.path.isdir(os.path.join(base_path, folder))]

first = True
folder_count = 0
for folder in folders:
    folder_count+=1
    folder_path = os.path.join(base_path, folder)
    files = os.listdir(folder_path)
    audio = None

    file_count = 0
    for file in files:
        file_count+=1
        filename = file.split('[')[0]
        # Show progress
        print(f'\r[{str(folder_count)+"/"+str(len(folders))   +"] ["+   str(file_count)+"/"+str(len(files))}]', end="")
        
        path = os.path.join('APNEA_EDF', folder, file)
        new_path = os.path.join('APNEA_DATA', folder, file)
        data, sound = edf_to_csv(path, timestamp_rate=timestamp_rate, stddev=stddev, fragment_n=file_count-1)

        if audio is None:
            audio = sound
        else:
            audio = np.concatenate((audio, sound))

        if first:
            first = False
            if not os.path.exists("APNEA_DATA/"+folder):
                os.makedirs("APNEA_DATA/"+folder)

            with open("APNEA_DATA/"+folder+"/"+filename+".csv", mode='w', newline='') as file_:
                writer = csv.writer(file_, delimiter=";")
                writer.writerow(['Timestamp', 'HR', 'SP02', 'MOVIMIENTO', 'TEMP'])
                writer.writerows(data)
        else:
            with open("APNEA_DATA/"+folder+"/"+filename+".csv", mode='a', newline='') as file_:
                writer = csv.writer(file_, delimiter=";")
                writer.writerows(data)

    first = True
    if save_audio:
        sf.write("APNEA_DATA/"+folder+"/"+filename+".flac", audio, 48000)
    annotate(filename, timestamp_rate)