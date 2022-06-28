#!bin/bash
start_time=`date "+%m/%d %H:%M:%S"`
#PREFIX_SIZES=(0 80 640)
PREFIX_SIZES=(0 10 20 40 80 160 320 640)
RANDOM_SIZES=(20)
#PREFIX_SIZES=(0 5 10 20 30 40 50 60 70 80)
#PREFIX_SIZES=(0)
#RANDOM_SIZES=(10 20 40 80 160 320 640)



exec_test() {
    for psize in "${PREFIX_SIZES[@]}"
        do
        for rsize in "${RANDOM_SIZES[@]}"
            do
              java -Xms1g -Xmx20g Evaluate $psize $rsize
            done
        done
}
echo $start_time
exec_test
end_time=`date "+%m/%d %H:%M:%S"`
echo start: $start_time
echo end: $end_time
diff=$(expr `date -d"$end_time" +%s` - `date -d"$start_time" +%s`)
echo "$diff"秒
echo `expr "$diff" / 60`分
