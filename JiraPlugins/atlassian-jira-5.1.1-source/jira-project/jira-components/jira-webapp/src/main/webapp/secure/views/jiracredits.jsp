<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <title><ww:text name="'about.jira.name'"/></title>
    <meta name="decorator" content="panel-general" />
</head>
<body>
<style type="text/css">

    #stage {
        background-color: #000;
        color:#0f0;
        font-family:monospace,serif;
        font-size: 20px;
        font-weight:bold;
        height:630px;
        line-height: 29px;
        margin:0 auto;
        overflow:hidden;
        position:relative;
        text-shadow: rgba(153, 208, 176, .7) 0px 0px 8px;
        width:1000px;
    }

    .crew {
        height:1000px;
        width:1000px;
        -moz-transform: rotate(270deg);
        -webkit-transform: rotate(270deg);
        -ms-transform: rotate(270deg);
        transform: rotate(270deg);
    }

    .role {
        margin-left: 1em;
    }

    @-moz-keyframes opac {
		    0%   { opacity: 1; }
		    100% { opacity: 0; }
		}
    @-webkit-keyframes opac {
		    0%   { opacity: 1; }
		    100% { opacity: 0; }
		}
    @-ms-keyframes opac {
		    0%   { opacity: 1; }
		    100% { opacity: 0; }
		}
    @-keyframes opac {
		    0%   { opacity: 1; }
		    100% { opacity: 0; }
		}
    @-moz-keyframes poz {
		   	from {left: 1100px;}
			to 	{left: -900px;}
		}
    @-webkit-keyframes poz {
		   	from {left: 1100px;}
			to 	{left: -900px;}
		}
    @-ms-keyframes poz {
		   	from {left: 1100px;}
			to 	{left: -900px;}
		}
    @keyframes poz {
		   	from {left: 1100px;}
			to 	{left: -900px;}
		}

    #stage li{
        list-style:none;
        left:1100px;
        margin-bottom:-16px; /* adjust this depending on numbers */
        position: relative;
        text-shadow: rgb(153, 208, 176) 0 0 8px;
        white-space:nowrap;

        /* animatrix */
        -moz-animation-name: poz, opac;
        -moz-animation-iteration-count: infinite;
        -moz-animation-direction: normal;
        -moz-animation-timing-function: ease-out;

        -webkit-animation-name: poz, opac;
        -webkit-animation-iteration-count: infinite;
        -webkit-animation-direction: normal;
        -webkit-animation-timing-function: ease-in;

        -ms-animation-name: poz, opac;
        -ms-animation-iteration-count: infinite;
        -ms-animation-direction: normal;
        -ms-animation-timing-function: ease-in;

        animation-name: poz, opac;
        animation-iteration-count: infinite;
        animation-direction: normal;
        animation-timing-function: ease-in;

    }

    #stage li:first-letter {
        color:#99d0b0;
        text-shadow: rgb(255, 255, 255) 5px 0 10px, rgb(255, 255, 255) 0 0 30px;
    }

    .c1{
        color: rgba(0, 255, 0, .5);
    }

        /* text size */
    .f09{
        font-size: .9em;
    }
    .f1 {
        font-size:1em;
    }
    .f12{
        font-size: 1.2em;
    }
    .f18{
        font-size: 1.8em;
    }
    .f2{
        font-size: 2em;
    }
    .f3{
        font-size: 2.2em;
    }
        /* animatrix duration */
    .d1{
        text-indent: 1em;
        -moz-animation-duration: 4s;
        -webkit-animation-duration: 4s;
        -ms-animation-duration: 4s;
        animation-duration: 4s;
    }
    .d2{
        text-indent: 3em;
        -moz-animation-duration: 6s;
        -webkit-animation-duration: 6s;
        -ms-animation-duration: 6s;
        animation-duration: 6s;
    }
    .d3{
        text-indent: 5em;
        -moz-animation-duration: 8s;
        -webkit-animation-duration: 8s;
        -ms-animation-duration: 8s;
        animation-duration: 8s;
    }
    .d4{
        text-indent: 8em;
        -moz-animation-duration: 10s;
        -webkit-animation-duration: 10s;
        -ms-animation-duration: 10s;
        animation-duration: 10s;
    }
        /* animatrix delay */
    .s1 {
        -moz-animation-delay: 1s;
        -webkit-animation-delay: 1s;
        -ms-animation-delay: 1s;
        animation-delay: 1s;
    }

    .s2 {
        -moz-animation-delay: 2s;
        -webkit-animation-delay: 2s;
        -ms-animation-delay: 2s;
        animation-delay: 2s;
    }
    .s3 {
        -moz-animation-delay: 3s;
        -webkit-animation-delay: 3s;
        -ms-animation-delay: 3s;
        animation-delay: 3s;
    }
    .s4 {
        -moz-animation-delay: 4s;
        -webkit-animation-delay: 4s;
        -ms-animation-delay: 4s;
        animation-delay: 4s;
    }
    .s5 {
        -moz-animation-delay: 5s;
        -webkit-animation-delay: 5s;
        -ms-animation-delay: 5s;
        animation-delay: 5s;
    }

        /* less capable browsers get the boring version */
    .msie-8 #stage,
    .msie-9 #stage {
        overflow-y: auto;
    }
    .msie-8 #stage li,
    .msie-9 #stage li {
        font-size: 1em !important;
        left:0;
        margin-bottom: 0 !important;
        text-indent: 0 !important;
    }
    .msie-9 .crew {
        -ms-transform: rotate(0deg);
    }


</style>

<div id="stage">
    <h1>JIRA 5.1 Credits</h1>

    <div id="credits">
        <ol class="crew">
            <li class="d4 f09 s2">Joshua Ali<span class="role">Graduate Java Developer</span></li>
            <li class="d2 c1 s3">Brenden Bain<span class="role">Senior Java Developer</span></li>
            <li class="d3 f1">Brad Baker<span class="role">Development Team Lead</span></li>
            <li class="d1 c1 s2">Stuart Bargon<span class="role">Scrum Master</span></li>
            <li class="d4 c1 f18">Veenu Bharara<span class="role">QA Engineer</span></li>
            <li class="d2 f09 s4">Antoine Busch<span class="role">Senior Java Developer</span></li>
            <li class="d1 c1">Trevor Campbell<span class="role">Senior Java Developer</span></li>
            <li class="d3 c1 s4">Ross Chaldecott<span class="role">Designer</span></li>
            <li class="d4 c1 f1">Tracy Chan<span class="role">Product Management Intern</span></li>
            <li class="d2 c1 f1">Panna Cherukuri<span class="role">QA Engineer</span></li>
            <li class="d1 f2">Jonathon Creenaune<span class="role">Development Team Lead</span></li>
            <li class="d2 s2 f18">Sean Curtis<span class="role">Frontend Developer</span></li>
            <li class="d3 c1 s5 f1">Chris Darroch<span class="role">Frontend Developer</span></li>
            <li class="d4 s3 f1">Gilmore Davidson<span class="role">JS Developer</span></li>
            <li class="d1 f09">Christopher Doble<span class="role">Graduate Java Developer</span></li>
            <li class="d1">Dave Elkan<span class="role">JS Developer</span></li>
            <li class="d2 c1 s3">Chris Fuller<span class="role">Senior Java Developer</span></li>
            <li class="d4 f09">Giles Gaskell<span class="role">Technical Writer</span></li>
            <li class="d3 s4">Slawek Ginter<span class="role">Senior Java Developer</span></li>
            <li class="d1 c1 f2 s3">Ian Grunert<span class="role">Java Developer</span></li>
            <li class="d1 s3">Shihab Hamid<span class="role">Development Team Lead</span></li>
            <li class="d2 s1 f3">Scott Harwood<span class="role">Senior JS Developer</span></li>
            <li class="d1 f1">James Hatherly<span class="role">Java Developer</span></li>
            <li class="d1">Adrian Hempel<span class="role">Developer</span></li>
            <li class="d3 s3">Alex Hennecke<span class="role">Developer</span></li>
            <li class="d1 s5 f1">Oswaldo Hernandez<span class="role">Java Developer</span></li>
            <li class="d1 c1 s2">Simone Houghton<span class="role">Program Manager</span></li>
            <li class="d4 s3 f1">Scott Hughes<span class="role">JS Developer</span></li>
            <li class="d3">Rosie Jameson<span class="role">Technical Writer</span></li>
            <li class="d4 c1 f09 s2">Jacek Jaroczynski<span class="role">Java Developer</span></li>
            <li class="d1">Bryce Johnson<span class="role">Build Engineer</span></li>
            <li class="d4 c1 f18">Martin Jopson<span class="role">Frontend Developer</span></li>
            <li class="d1 c1 s2">Andreas Knecht<span class="role">Development Team Lead</span></li>
            <li class="d4 c1 s4">Dariusz Kordonski<span class="role">Java Developer</span></li>
            <li class="d2 c1">Roy Krishna<span class="role">Product Manager</span></li>
            <li class="d4 c1 f18">Peggy Kuo<span class="role">Developer</span></li>
            <li class="d3 c1 f2">Mark Lassau<span class="role">Development Team Lead</span></li>
            <li class="d4 c1 f1">Alex Manusu<span class="role">Product Management Intern</span></li>
            <li class="d1 c1">Martin Meinhold<span class="role">Java Developer</span></li>
            <li class="d1 f3 s2">Nick Menere<span class="role">Development Team Lead</span></li>
            <li class="d3 f2 s1">Aleksander Mierzwicki<span class="role">Developer</span></li>
            <li class="d2 c1 f2">Luis Miranda<span class="role">Senior Java Developer</span></li>
            <li class="d1 f09">Joseph Molloy<span class="role">Student Developer</span></li>
            <li class="d4 c1">Chris Mountford<span class="role">Senior Software Developer</span></li>
            <li class="d2 c1 s3">Pawel Niewiadomski<span class="role">Senior Java Developer</span></li>
            <li class="d1 c1 f12">Olli Nevalainen<span class="role">Developer</span></li>
            <li class="d3 c1 s5">Ken Olofsen<span class="role">Marketing Manager</span></li>
            <li class="d1 c1 f2 s3">Michal Orzechowski<span class="role">Java Developer</span></li>
            <li class="d1 c1 s2">Justus Pendleton<span class="role">Development Team Lead</span></li>
            <li class="d3 c1 s4">Matt Quail<span class="role">Product Architect</span></li>
            <li class="d4 c1 f1">Jonathan Raoult<span class="role">Senior Java Developer</span></li>
            <li class="d1 c1">Jay Rogers<span class="role">UI Designer</span></li>
            <li class="d4 f1 s2">Bryan Rollins<span class="role">Group Product Manager</span></li>
            <li class="d1 f12">Michael Ruflin<span class="role">Developer</span></li>
            <li class="d4">Felix Schmitz<span class="role">Developer</span></li>
            <li class="d1 f18">Wojciech Seliga<span class="role">Development Team Lead</span></li>
            <li class="d2 s5 f3">Mike Sharp<span class="role">Design Engineer</span></li>
            <li class="d1 f1 s3">Kiran Shekhar<span class="role">QA Engineer</span></li>
            <li class="d2">Paul Slade<span class="role">Manager JIRA</span></li>
            <li class="d3">Robert Smart<span class="role">Senior Java Developer</span></li>
            <li class="d1 c1 f2 s3">Graeme Smith<span class="role">Java Developer</span></li>
            <li class="d1 c1">Min'an Tan<span class="role">Developer</span></li>
            <li class="d1 f1">David Tang<span class="role">Graduate JS Developer</span></li>
            <li class="d4 s5 f2">Samantha Thebridge<span class="role">User Interaction Designer</span></li>
            <li class="d2 c1 s3">Michael Tokar<span class="role">Developer</span></li>
            <li class="d3 c1 f12 s2">James Winters<span class="role">Development Team Lead</span></li>
            <li class="d1 c1 s2 f2">Edwin Wong<span class="role">Senior Product Manager</span></li>
            <li class="d4 c1 f1">Geoffrey Wong<span class="role">Graduate QA Engineer</span></li>
            <li class="d2 c1 s4">James Wong<span class="role">Developer</span></li>
            <li class="d1 c1 f2">Penny Wyatt<span class="role">QA Team Lead</span></li>
            <li class="d1 s5 f1">Michal Zeglarski<span class="role">Developer</span></li>
            <li class="d4 c1 f1">Edward Zhang<span class="role">Graduate Developer</span></li>
        </ol>
    </div>
</div>
</body>
</html>
