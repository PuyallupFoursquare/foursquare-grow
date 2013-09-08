<#macro commonpage>
    <!doctype html>
    <html>
    <head>
        <title>Grow Process</title>

        <link href="http://fonts.googleapis.com/css?family=Arvo:400,700" rel="stylesheet">
        <link rel="stylesheet" href="${staticRoot}/style.css" />
        <script src="${staticRoot}/scripts/jquery.min.js"></script>
        <script src="${staticRoot}/scripts/jquery-ui.js"></script>
        <script src="${staticRoot}/scripts/growth.js"></script>
    </head>
    <body>
    <div id="notfooter">
        <#include "/templates/nav.ftl">

        <#nested>

        <div id="push"></div>
    </div>

    <#include "/templates/footer.ftl">

    </body>
    </html>
</#macro>
