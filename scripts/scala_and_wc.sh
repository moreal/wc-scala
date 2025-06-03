# Expected usage
# cat fixtures/foo | ./scripts/scala_and_wc.sh -w
# cat fixtures/bar | ./scripts/scala_and_wc.sh -w -c -l
# cat fixtures/bar | ./scripts/scala_and_wc.sh -w -m -l

temp_file=$(mktemp)
cat > "$temp_file"

echo "Running: scala run main.scala -- $@"
scala run main.scala -- "$@" < "$temp_file"

echo "Running: wc $@"
wc "$@" < "$temp_file"

rm "$temp_file"
