import os
import numpy as np
import matplotlib.pyplot as plt

'''
    DATA OF ALl ROBOT TYPES 2D LIST
    @:type list []
'''
ELC = []
POL = []
SLD = []
MCR = []
ELC_AVG = []
POL_AVG = []
SLD_AVG = []
MCR_AVG = []
ELC_TOTAL = []
POL_TOTAL = []
SLD_TOTAL = []
MCR_TOTAL = []
ELC_IDs = []
POL_IDs = []
SLD_IDs = []
MCR_IDs = []

BYTE_CODE_LIMITS = {'ELC': 20000, 'POL': 15000, 'SLD': 7500, 'MCR': 15000}


def read_file():
    with open("output.txt") as f:
        for line in f:
            line = line[0:len(line) - 1]
            if line.find("$ELC") != -1:
                index = line.index("$ELC")
                bytes_used, round_no, correct_round_no, id = map(int, line[index + 5:].split(" "))
                ELC.append([correct_round_no, round_no, bytes_used, id])
            elif line.find("$POL") != -1:
                index = line.index("$POL")
                bytes_used, round_no, correct_round_no, id = map(int, line[index + 5:].split(" "))
                POL.append([correct_round_no, round_no, bytes_used, id])
            elif line.find("$SLD") != -1:
                index = line.index("$SLD")
                bytes_used, round_no, correct_round_no, id = map(int, line[index + 5:].split(" "))
                SLD.append([correct_round_no, round_no, bytes_used, id])
            elif line.find("$MCR") != -1:
                index = line.index("$MCR")
                bytes_used, round_no, correct_round_no, id = map(int, line[index + 5:].split(" "))
                MCR.append([correct_round_no, round_no, bytes_used, id])

def plot_averaged():
    # print(ELC_TOTAL)
    # print(ELC_IDs)
    # print(ELC_AVG)
    plt.plot(ELC_AVG)
    plt.ylabel("ByteCode used")
    plt.xlabel("round number")
    plt.savefig('plots/ELC_average_all_rounds.png')
    plt.show()

    plt.plot(POL_AVG)
    plt.ylabel("ByteCode used")
    plt.xlabel("round number")
    plt.savefig('plots/POL_average_all_rounds.png')
    plt.show()

    plt.plot(SLD_AVG)
    plt.ylabel("ByteCode used")
    plt.xlabel("round number")
    plt.savefig('plots/SLD_average_all_rounds.png')
    plt.show()

    plt.plot(MCR_AVG)
    plt.ylabel("ByteCode used")
    plt.xlabel("round number")
    plt.savefig('plots/MCR_average_all_rounds.png')
    plt.show()

def format_data(log_file):
    print(ELC)
    TOTAL_BOTS_AVG = [0 for _ in range(1501)]
    ELC_TOTAL = [0 for _ in range(1501)]
    POL_TOTAL = [0 for _ in range(1501)]
    SLD_TOTAL = [0 for _ in range(1501)]
    MCR_TOTAL = [0 for _ in range(1501)]
    ELC_IDs = [set() for _ in range(1501)]
    POL_IDs = [set() for _ in range(1501)]
    SLD_IDs = [set() for _ in range(1501)]
    MCR_IDs = [set() for _ in range(1501)]
    for element in ELC:
        if element[0] != element[1]:
            byte_code_used = element[2] + (element[0] - element[1]) * BYTE_CODE_LIMITS['ELC']
            log_file.write("Excess byte code ELC-{id} at round-{round_no} total-byte-code:{byte}\n".format(id=element[3], round_no=element[1], byte=byte_code_used))
        ELC_TOTAL[element[1]] += element[2]
        ELC_IDs[element[1]].add(element[3])

    for element in POL:
        if element[0] != element[1]:
            byte_code_used = element[2] + (element[0] - element[1]) * BYTE_CODE_LIMITS['POL']
            log_file.write(
                "Excess byte code Politician-{id} at round-{round_no} total-byte-code:{byte}\n".format(id=element[3], round_no=element[1], byte=byte_code_used))
        POL_TOTAL[element[1]] += element[2]
        POL_IDs[element[1]].add(element[3])

    for element in SLD:
        if element[0] != element[1]:
            byte_code_used = element[2] + (element[0] - element[1]) * BYTE_CODE_LIMITS['SLD']
            log_file.write(
                "Excess byte code Slanderer-{id} at round-{round_no} total-byte-code:{byte}\n".format(id=element[3], round_no=element[1], byte=byte_code_used))
        SLD_TOTAL[element[1]] += element[2]
        SLD_IDs[element[1]].add(element[3])

    for element in MCR:
        if element[0] != element[1]:
            byte_code_used = element[2] + (element[0] - element[1]) * BYTE_CODE_LIMITS['MCR']
            log_file.write(
                "Excess byte code mucracker-{id} at round-{round_no} total-byte-code:{byte}\n".format(id=element[3], round_no=element[1], byte=byte_code_used))
        MCR_TOTAL[element[1]] += element[2]
        MCR_IDs[element[1]].add(element[3])

    for i in range(0, 1501):
        ELC_AVG.append(int(ELC_TOTAL[i] / (1 if len(ELC_IDs[i]) == 0 else len(ELC_IDs[i]))))
        POL_AVG.append(int(POL_TOTAL[i] / (1 if len(POL_IDs[i]) == 0 else len(POL_IDs[i]))))
        SLD_AVG.append(int(SLD_TOTAL[i] / (1 if len(SLD_IDs[i]) == 0 else len(SLD_IDs[i]))))
        MCR_AVG.append(int(MCR_TOTAL[i] / (1 if len(MCR_IDs[i]) == 0 else len(MCR_IDs[i]))))

    plot_averaged()


if __name__ == "__main__":
    read_file()
    log_file = open('plots/logs.txt', 'w')
    format_data(log_file=log_file)
    log_file.close()
