The .zip "Experiments" contains one folder for each section.
In case of more subsection it has annidate folders. Then, in most of the cases, there are two folders, one for the read case, the other for the write case. In each of read/write there are the different devices involved in the experiment, e.g. memtier clients etc, that have all the experiments in one file following the specifics that you required.
Other files present are the dstat, iperf and ping collections.
The experiments are concatenated in the files "client_nn.log" and "mw_NN.log".
In order to address the data to the files, I concatenated the filename and the content.
The filename is standard: "E{NUM_EXP}{WorkloadMode}OC{ClientMachine}I{Instance}_T{NUM_THREADS}_C{NUM_CLIENTS}_TRY{Num try}.log"

The values in the report were calculated with the jupyter scripts. These notebook can be run directly in the folder were they belong, they will automatically access all the files they need and output the values.

As for section 7, in order to understand the results shown in the table, looking at the bottom of the .py file will help.
In fact, both for the M/M/.. queues, and for the network of queue, the results are calculated at the bottom of the scripts.

