import os
import csv
import numpy as np

def annotate(file, timestamp_rate):
    
    # Obtain full path of the files
    rml_filename = os.path.join('APNEA_RML',file+'.rml')
    csv_filename = os.path.join("APNEA_DATA", file, file+'.csv')
    
    # Read the files
    with open(rml_filename, 'r') as file:
        strlines = file.readlines()
    
    with open(csv_filename, 'r') as file:
        reader = csv.reader(file, delimiter=";")
        data = list(reader)

    # Create a array and fill it with the rml data
    apnea = np.zeros(len(data*timestamp_rate)-1)

    for line in strlines:
        temp = line.split()
        if len(temp) > 1:
            if temp[0] == '<Event':
                familyv = temp[1].split('=')
                family = familyv[1].strip('"')
                typev = temp[2].split('=')
                type = typev[1].strip('"')
                startv = temp[3].split('=')
                start = float(startv[1].strip('"'))
                durationv = temp[4].split('=')
                duration = float(durationv[1].replace(">","").strip('"'))
                if family == "Respiratory":
                    if type == "Hypopnea": 
                        apnea[int(start):int(start+duration)] = 1
                    if type == "ObstructiveApnea": 
                        apnea[int(start):int(start+duration)] = 1 #2
                    if type == "MixedApnea": 
                        apnea[int(start):int(start+duration)] = 1 #3
                    if type == "CentralApnea": 
                        apnea[int(start):int(start+duration)] = 1 #4
    
    data[0].append("LABEL")
    for i in range(len(data)-1):
        apnea_type = 0
        if 1 in apnea[i*timestamp_rate:(i+1)*timestamp_rate]:
            apnea_type = 1
        # if 2 in apnea[i*timestamp_rate:(i+1)*timestamp_rate]:
        #     apnea_type = 2
        # if 3 in apnea[i*timestamp_rate:(i+1)*timestamp_rate]:
        #     apnea_type = 3
        # if 4 in apnea[i*timestamp_rate:(i+1)*timestamp_rate]:
        #     apnea_type = 4
        data[i+1].append(apnea_type)

    # Write the annotated data into the csv file
    with open(csv_filename, 'w', newline='') as file:
        writer = csv.writer(file, delimiter=";")
        writer.writerows(data)
                
    return data
