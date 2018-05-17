<?xml version="1.0" encoding="UTF-8"?>
<result>
<#list forms as form>
  <form>
     <application-name>${form.orbeon.app}</application-name>
     <form-name>${form.orbeon.formName}</form-name>
     <title xml:lang="en">${form.dublincore.title}</title>
     <last-modified-time>{form.dublincore.modified.time?iso_utc}</last-modified-time>  
  </form>
</#list>
</result>

