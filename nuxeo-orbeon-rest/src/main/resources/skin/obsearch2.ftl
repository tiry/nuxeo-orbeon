<?xml version="1.0" encoding="UTF-8"?>
<documents search-total="${forms?size}" page-size="10" page-number="1" query="">
<#list forms as form>
	<document created="${form.dublincore.created?datetime?string.iso}"
              last-modified="${form.dublincore.modified?datetime?string.iso}"
              name="${form.orbeon.formId}"
              draft="false">
              <details>
                <detail>${form.dublincore.title}</detail>
              </details>
    </document>
</#list>
</documents>

