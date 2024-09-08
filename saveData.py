import csv

def text_to_csv(input_file, output_file):
    with open(input_file, 'r') as txt_file:
        lines = txt_file.readlines()

    data = []
    for line in lines:
        if "startTime:" in line and "endTime:" in line:

            start_time = int(line.split(":")[1].split("endTime")[0].strip())
            end_time = int(line.split("endTime:")[1].strip())
            latency = (end_time - start_time) / 1000000
            data.append([start_time, end_time, latency])


    with open(output_file, 'w', newline='') as csv_file:
        writer = csv.writer(csv_file)
        writer.writerow(["startTime", "endTime", "latency"])
        writer.writerows(data)

input_file = "data.txt"
output_file = "data.csv"

text_to_csv(input_file, output_file)
