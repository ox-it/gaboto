all: pdf html

html:
	xsltproc --xinclude --stringparam OUCSBASE http://www.oucs.ox.ac.uk \
	http://www.oucs.ox.ac.uk/stylesheet/oucs/oucsp5-twocol.xsl \
	user-manual.xml >user-manual.html

tex:
	xsltproc --xinclude \
	print.xsl user-manual.xml >user-manual.tex

pdf: tex
	xelatex user-manual.tex
	xelatex user-manual.tex
	rm -f user-manual.tex user-manual.aux user-manual.log user-manual.out

commit: all
	svn -m update commit user-manual.html user-manual.pdf
