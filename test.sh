#!bin/bash
start_time=`date "+%m/%d %H:%M:%S"`
#PREFIX_SIZES=(0 80 640)
PREFIX_SIZES=(0 10 20 40 80 160 320 640)
#REFIX_SIZES=(0 5 10 20 30 40 50 60 70 80)

exec_test() {
    for size in "${PREFIX_SIZES[@]}"
    do
      java -Xms1g -Xmx12g Evaluate $size
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
