<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <title><ww:text name="'about.jira.name'"/></title>
    <meta name="decorator" content="popup">
</head>

<body>
<style type="text/css">

    #stage {
        background:#000 url("<%= request.getContextPath() %>/images/stars.png") repeat 0 0;
        font-family:"Trebuchet MS", Helvetica, sans-serif;height:630px;margin:0 auto 0;overflow:hidden;position:relative;text-align:center;width:1020px;
	}
    #stage h1,
    #stage h2 {text-transform:uppercase;}
    #jiraLogo {position:absolute;width:1000px;}
    #intro {color:#52e4fe;font-size:48px;line-height:1.5;width:50%;}
    #credits {color:#ff0;width:500px;}
    #credits p {text-align:justify;}

	.crew {padding-left:150px;}
	.crew dt {clear:left;float:left;margin:0 0 5px -150px;text-align:left;}
	.crew dd {margin-bottom:5px;text-transform:uppercase;text-align:right;}

/*
At the time of release browser support for CSS perspective is very limited.
So for the richest viewing experience use Safari. M.
*/
    .safari .version {font-size:96px;}
	.safari #stage  .title {font-size:72px;}
	.safari #stage h3 {font-size:56px;}
	.safari #stage {
		-webkit-perspective:700;
		-webkit-perspective-origin:50% 10%;
	}

    /* intro */
	@-webkit-keyframes introFade {
		0% { opacity:0; }
		10%   { opacity:1; }
		90%   { opacity:1; }
		100%   { opacity:0; }
	}
	.safari  #intro {margin:150px auto 0;opacity:0;
		-webkit-animation:introFade 5s linear;
		-webkit-animation-delay:4s;
		-webkit-animation-iteration-count: 1;
	}


	/* JIRA logo */
	@-webkit-keyframes jiraLogo {
		0% { -webkit-transform: translateZ(0); opacity:1;}
		50% { -webkit-transform: translateZ(-50000px); opacity:1; }
		60% { -webkit-transform: translateZ(-60000px); opacity:0; }
		100%   { -webkit-transform: translateZ(-100000px); opacity:0;}
	}
	.safari #jiraLogo {opacity:0;position:absolute;top:100px;
		-webkit-transform-origin:center center;
		-webkit-animation:jiraLogo 15s linear;
		-webkit-animation-delay: 10s;
		-webkit-animation-iteration-count: 1;
		-webkit-animation-timing-function: ease-in;
	}


	/* credits */
	@-webkit-keyframes credits {
		0% { -webkit-transform:rotateX(80deg) translateZ(200px) translateY(900px);opacity:1;}
		95% { -webkit-transform:rotateX(80deg) translateZ(200px) translateY(-3200px);opacity:1;}
		100% { -webkit-transform:rotateX(80deg) translateZ(200px) translateY(-3500px);opacity:0;}
	}
	.safari #credits {font-size:48px;opacity:0;width:100%;
		-webkit-animation:credits 40s linear;
		-webkit-animation-delay:9s;
		-webkit-animation-iteration-count: 1;
		-webkit-transform-style:preserve-3d;
	}

/* js fallback needs these  */
    .mozilla #intro,
    .chrome #intro,
    .msie #intro,
    .mozilla #credits,
    .chrome #credits,
    .msie #credits,
    .mozilla #jiraLogo,
    .chrome #jiraLogo,
    .msie #jiraLogo {display:none;position:absolute;left:50%;top:50%;}

</style>

<div id="stage">
    <h1><ww:text name="'jira.credits'"/></h1>
    <img alt="JIRA" src="<%= request.getContextPath() %>/images/JIRAwars.png" id="jiraLogo" />
    <p id="intro">
		A long time ago<br>
		(well, about 9 years)<br>
		in a city far, far away...
	</p>
    <div id="credits">
		<h1 class="version">Episode IV.IV</h1>
		<h2 class="title">A New Administrator</h2>
		<dl class="crew">
            <dt>Brenden	Bain</dt><dd>Developer</dd>
            <dt>Brad	Baker</dt><dd>Developer</dd>
            <dt>Stuart	Bargon</dt><dd>Scrum Master</dd>
            <dt>Veenu	Bharara</dt><dd>QA Engineer</dd>
            <dt>Trevor	Campbell</dt><dd>Developer</dd>
            <dt>Ross    Chaldecott</dt><dd>Designer</dd>
            <dt>Sean	Curtis</dt><dd>Frontend Developer</dd>
            <dt>Dave	Elkan</dt><dd>Javascript Developer</dd>
            <dt>Giles   Gaskell</dt><dd>Technical Writer</dd>
            <dt>Ian	    Grunert</dt><dd>Graduate Java Developer</dd>
            <dt>Scott	Harwood</dt><dd>Javascript Developer</dd>
            <dt>Alex	Hennecke</dt><dd>Developer</dd>
            <dt>Oswaldo	Hernandez</dt><dd>Developer</dd>
            <dt>Scott	Hughes</dt><dd>JavaScript Developer</dd>
            <dt>Rosie   Jameson</dt><dd>Technical Writer</dd>
            <dt>Bryce	Johnson</dt><dd>Build Engineer</dd>
            <dt>Martin	Jopson</dt><dd>Frontend Developer</dd>
            <dt>Andreas	Knecht</dt><dd>Developer</dd>
            <dt>Dariusz	Kordonski</dt><dd>Developer</dd>
            <dt>Roy     Krishna</dt><dd>Product Manager</dd>
            <dt>Mark	Lassau</dt><dd>Developer</dd>
            <dt>Peter	Leschev</dt><dd>Developer</dd>
            <dt>Nick	Menere</dt><dd>Developer</dd>
            <dt>Luis	Miranda</dt><dd>Developer</dd>
            <dt>Chris	Mountford</dt><dd>Developer</dd>
            <dt>Ken	    Olofsen</dt><dd>Product Marketing Manager</dd>
            <dt>Justus	Pendleton</dt><dd>Developer</dd>
            <dt>Matt	Quail</dt><dd>Developer</dd>
            <dt>Jay	    Rogers</dt><dd>Design Manager</dd>
            <dt>Bryan	Rollins</dt><dd>Group Product Manager</dd>
            <dt>Michael	Ruflin</dt><dd>Developer</dd>
            <dt>Felix	Schmitz</dt><dd>Developer</dd>
            <dt>Mike	Sharp</dt><dd>Design Engineer</dd>
            <dt>Rupert	Shuttleworth</dt><dd>Graduate Java Developer</dd>
            <dt>Paul	Slade</dt><dd>Manager JIRA</dd>
            <dt>Robert	Smart</dt><dd>Developer</dd>
            <dt>Graeme  Smith</dt><dd>Developer</dd>
            <dt>Min'an	Tan</dt><dd>Developer</dd>
            <dt>Jason	Taylor</dt><dd>Interface Designer</dd>
            <dt>Michael	Tokar</dt><dd>Developer</dd>
            <dt>James	Winters</dt><dd>Developer</dd>
            <dt>Edwin	Wong</dt><dd>JIRA Product Manager</dd>
            <dt>Penny	Wyatt</dt><dd>QA Engineer</dd>
            <dt>Edward	Zhang</dt><dd>Graduate Developer</dd>
		</dl>
	</div>
</div>
</body>
</html>
