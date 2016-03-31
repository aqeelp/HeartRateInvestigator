BEGIN {
	print "["
}
{
	if ($0 == "}{") {
		print "},{"
	} else {
		print $0
	}
}
END {
	print "]"
}