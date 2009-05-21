TALKS=gaboto
default: clean pdfslides slides 



slides:
ifeq "$(TALKS)" ""
	@echo 'I was expecting a TALKS parameter such as in: make TALKS=1a'
	@exit 1 
endif
	for i in $(TALKS); do \
		xsltproc \
		--xinclude \
		--stringparam cssFile teislides.css \
		--stringparam showNamespaceDecls false \
		--stringparam masterFile $$i- \
		/usr/share/xml/tei/stylesheet/slides/teihtml-slides.xsl \
		$$i.xml;\
	done  

pdfslides:
ifeq "$(TALKS)" ""
	@echo 'I was expecting a TALKS parameter such as in: make TALKS=1a'
	@exit 1 
endif
	for i in $(TALKS); do \
		xsltproc --xinclude -o $$i.tex \
		slides.xsl \
		$$i.xml ; \
		xelatex -interaction=batchmode $$i.tex ;\
		xelatex -interaction=batchmode $$i.tex ; done


handout:
	for i in $(TALKS); do \
		xsltproc --stringparam latexLogo ../logo \
		-o $$i-handout.tex \
		/usr/share/xml/tei/stylesheet/latex/tei.xsl \
		$$i.xml ; \
		xelatex $$i-handout.tex ; \
		xelatex $$i-handout.tex ; \
        done

clean:
	-rm *.aux
	-rm *.log
	-rm *.nav
	-rm *.out
	-rm *.snm
	-rm *.tex
	-rm *.toc
	-rm *.vrb
	-rm *~
	for i in ${TALKS}; do \
	test -f $$i.xml && rm -f $$i.pdf $$i-handout.pdf ; done

