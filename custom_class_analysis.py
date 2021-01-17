import os
import sys
import matplotlib.pyplot as plt
import numpy as np
import math

Data = {}
SIZE = 150
NUM_BARS = int(1500 / SIZE)


def dataLoader():
    with open("output1.txt") as f:
        line_idx = 0
        lines = f.readlines()
        print(type(lines))
        while line_idx < len(lines):
            line = lines[line_idx][:-1]
            if line.find("@@") != -1:
                index = line.index("@@")
                round_no, log_count, class_name = map(str, line[index + 2:].split(" "))
                round_no = int(round_no)
                log_count = int(log_count)
                line_idx += 1
                if class_name not in Data.keys():
                    Data[class_name] = {}
                cnt = 0
                while cnt < log_count:
                    line = lines[line_idx][: -1]
                    Function, Byte_used = map(str, line.split(" "))
                    Byte_used = int(Byte_used)
                    if Function in Data[class_name].keys():
                        Data[class_name][Function].append([round_no, Byte_used])
                    else:
                        Data[class_name][Function] = []
                        Data[class_name][Function].append([round_no, Byte_used])
                    cnt += 1
                    line_idx += 1
            else:
                line_idx += 1


def sort_and_plot(listed_data, function_name, class_name, PATH):
    listed_data.sort(key=lambda x: x[0])
    bytes = [0 for _ in range(1501)]
    cnt = [0 for _ in range(1501)]
    # for average
    # for el in listed_data:
    #     cnt[el[0]] += 1
    #     bytes[el[0]] += el[1]
    #
    # for i in range(0, 1501):
    #     cnt[i] = max(cnt[i], 1)
    #     bytes[i] = int(bytes[i]/cnt[i])

    for el in listed_data:
        bytes[el[0]] = max(el[1], bytes[el[0]])
    X = []
    Y = [0 for _ in range(NUM_BARS)]
    l = 0
    r = SIZE
    for i in range(0, NUM_BARS):
        for i1 in range(l, r + 1):
            Y[i] = max(Y[i], bytes[i1])
        X.append(str(l) + "-" + str(r))
        l += SIZE
        r += SIZE
    print(X)
    print(Y)
    # sys.exit()
    fig = plt.figure(figsize=(10, 5))
    # ax = fig.add_axes([0, 0, 1, 1])
    plt.xlabel("Max BC used by a bot in class-" + class_name + " by fn-" + function_name)
    plt.ylabel("ByteCode")
    plt.bar(X, Y, color='green', width=0.4)
    SAVE_PATH = os.path.join(PATH, function_name)
    plt.savefig(SAVE_PATH)
    plt.show()


def plot_data():
    PLOT_PATH = os.path.join(os.getcwd(), "plots")
    for category in Data.keys():
        s = ""
        CUR_PATH = os.path.join(PLOT_PATH, category)
        if not os.path.isdir(CUR_PATH):
            os.makedirs(CUR_PATH)
        for fns in Data[category].keys():
            s += fns + " "
        print("Plotting for {CAT} having functions ".format(CAT=category) + s)
        for fns in Data[category].keys():
            listed_data = Data[category][fns]
            sort_and_plot(listed_data, fns, category, CUR_PATH)


if __name__ == "__main__":
    dataLoader()
    plot_data()
