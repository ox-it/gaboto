<xsl:stylesheet 
 xmlns:tei="http://www.tei-c.org/ns/1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 version="1.0">
<xsl:import href="/usr/share/xml/tei/stylesheet/latex/tei.xsl"/>
<xsl:param name="reencode">false</xsl:param>

<xsl:variable name="docClass">book</xsl:variable>
<xsl:template name="latexPreambleHook">
\usepackage{makeidx}
\makeindex
%\setmonofont{DejaVu Sans Mono}
%\setromanfont{Minion Pro}
%\setmonofont{CourierStd}
%\setsansfont{Myriad Pro}
\setlength{\headheight}{14pt}
</xsl:template>


<xsl:template name="latexBegin">
<xsl:text>\makeatletter
\thispagestyle{plain}</xsl:text>
<xsl:if test="not(tei:text/tei:front/tei:titlePage)">
  <xsl:call-template name="printTitleAndLogo"/>
</xsl:if>
<xsl:text>\markright{\@title}%
\markboth{\@title}{\@author}%
\fvset{frame=single,numberblanklines=false,xleftmargin=5mm,xrightmargin=5mm}
\fancyhf{} 
\setlength{\headheight}{14pt}
\fancyhead[LE]{\bfseries\leftmark} 
\fancyhead[RO]{\bfseries\rightmark} 
\fancyfoot[RO]{}
\fancyfoot[CO]{\thepage}
\fancyfoot[LO]{}
\fancyfoot[LE]{}
\fancyfoot[CE]{\thepage}
\fancyfoot[RE]{}
\hypersetup{linkbordercolor=0.75 0.75 0.75,urlbordercolor=0.75 0.75 0.75,bookmarksnumbered=true,letterpaper}
\def\l@section{\@dottedtocline{1}{3em}{2.3em}}
\def\l@subsection{\@dottedtocline{2}{4em}{3.2em}}
\def\l@subsubsection{\@dottedtocline{3}{5em}{4.1em}}
\def\l@paragraph{\@dottedtocline{4}{6em}{6em}}
\def\l@subparagraph{\@dottedtocline{5}{7em}{6em}}
\def\@pnumwidth{3em}
\setcounter{tocdepth}{2}
\def\tableofcontents{
\clearpage
\pdfbookmark[0]{Table of Contents}{TOC}
\hypertarget{TOC}{}
\section*{\contentsname}\@starttoc{toc}}
\fancypagestyle{plain}{\fancyhead{}\renewcommand{\headrulewidth}{0pt}}
\def\chaptermark#1{\markboth {\thechapter. \ #1}{}}
\def\sectionmark#1{\markright { \ifnum \c@secnumdepth >\z@
          \thesection. \ %
        \fi
	#1}}
\def\egxmlcite#1{\raisebox{12pt}[0pt][0pt]{\parbox{.95\textwidth}{\raggedleft #1}}}
\def\oddindex#1{{\bfseries\hyperpage{#1}}}
\def\exampleindex#1{{\itshape\hyperpage{#1}}}
\def\mainexampleindex#1{{\bfseries\itshape\hyperpage{#1}}}
\setlength{\leftmargini}{2\parindent}%
\renewcommand{\@listI}{%
   \setlength{\leftmargin}{\leftmargini}%
   \setlength{\topsep}{\medskipamount}%
   \setlength{\itemsep}{0pt}%
   \setlength{\listparindent}{1em}%
   \setlength{\rightskip}{1em}%
}
\renewcommand\normalsize{\@setfontsize\normalsize{10}{12}%
  \abovedisplayskip 10\p@ plus2\p@ minus5\p@
  \belowdisplayskip \abovedisplayskip
  \abovedisplayshortskip  \z@ plus3\p@
  \belowdisplayshortskip  6\p@ plus3\p@ minus3\p@
  \let\@listi\@listI
}
\renewcommand\small{\@setfontsize\small{9pt}{11pt}%
   \abovedisplayskip 8.5\p@ plus3\p@ minus4\p@
   \belowdisplayskip \abovedisplayskip
   \abovedisplayshortskip \z@ plus2\p@
   \belowdisplayshortskip 4\p@ plus2\p@ minus2\p@
   \def\@listi{\leftmargin\leftmargini
               \topsep 2\p@ plus1\p@ minus1\p@
               \parsep 2\p@ plus\p@ minus\p@
               \itemsep 1pt}
}
\renewcommand\footnotesize{\@setfontsize\footnotesize{8}{9.5}%
  \abovedisplayskip 6\p@ plus2\p@ minus4\p@
  \belowdisplayskip \abovedisplayskip
  \abovedisplayshortskip \z@ plus\p@
  \belowdisplayshortskip 3\p@ plus\p@ minus2\p@
  \def\@listi{\leftmargin\leftmargini
              \topsep 2\p@ plus\p@ minus\p@
              \parsep 2\p@ plus\p@ minus\p@
              \itemsep \parsep}
}
\renewcommand\scriptsize{\@setfontsize\scriptsize{7}{8}}
\renewcommand\tiny{\@setfontsize\tiny{5}{6}}
\renewcommand\large{\@setfontsize\large{12}{14.4}}
\renewcommand\Large{\@setfontsize\Large{14.4}{18}}
\renewcommand\LARGE{\@setfontsize\LARGE{17.28}{22}}
\renewcommand\huge{\@setfontsize\huge{20.74}{25}}
\renewcommand\Huge{\@setfontsize\Huge\@xxvpt{30}}
\parskip3pt
\parindent0em
% for refdocs
\renewenvironment{itemize}{%
  \advance\@itemdepth \@ne
  \edef\@itemitem{labelitem\romannumeral\the\@itemdepth}%
  \begin{list}{\csname\@itemitem\endcsname}
  {%
   \setlength{\leftmargin}{\parindent}%
   \setlength{\labelwidth}{.7\parindent}%
   \setlength{\topsep}{2pt}%
   \setlength{\itemsep}{2pt}%
   \setlength{\itemindent}{2pt}%
   \setlength{\parskip}{0pt}%
   \setlength{\parsep}{2pt}%
   \def\makelabel##1{\hfil##1\hfil}}%
  }
  {\end{list}}
\fancyhfoffset[LO,LE]{2em}
\renewcommand\section{\@startsection {section}{1}{-2em}%
     {-1.75ex \@plus -0.5ex \@minus -.2ex}%
     {0.5ex \@plus .2ex}%
     {\reset@font\Large\bfseries\sffamily}}
\renewcommand\subsection{\@startsection{subsection}{2}{-2em}%
     {-1.75ex\@plus -0.5ex \@minus- .2ex}%
     {0.5ex \@plus .2ex}%
     {\reset@font\Large\sffamily}}
\makeatother </xsl:text>
<xsl:call-template name="beginDocumentHook"/>
</xsl:template>

<xsl:param name="latexGeometryOptions">twoside,letterpaper,lmargin=1in,rmargin=1in,tmargin=1in,bmargin=1in</xsl:param>


</xsl:stylesheet>
