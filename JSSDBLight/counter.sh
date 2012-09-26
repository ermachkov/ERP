#!/bin/bash

count=0;

while read LINE
   do
      echo $LINE
      counter=$(wc -l < $LINE )
      echo $counter
      let count=$counter+count;
   done < out.txt

echo $count;
