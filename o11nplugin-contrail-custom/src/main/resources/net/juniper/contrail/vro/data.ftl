<#ftl strip_whitespace=true>
package ${packageName};

import ch.dunes.vso.sdk.api.IPluginFactory;
import com.vmware.o11n.sdk.modeldriven.*;
import org.springframework.beans.factory.BeanFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.stream.Collectors;
import net.juniper.contrail.vro.model.Connection;
import net.juniper.contrail.vro.model.Executor;
import net.juniper.contrail.vro.format.*;
import net.juniper.contrail.api.*;
import net.juniper.contrail.api.types.*;

<@compress single_line=true>
public class ${className}
<#if fullSuperClassName??>
    extends ${fullSuperClassName}
<#else>
    extends AbstractWrapper
</#if>
    implements ModelWrapper <#if findable>, Findable<#elseif rootIdPropagated>, RootIdPropagator</#if>  {
</@compress>

    private static final long serialVersionUID = 1L;
    <#if objectClass || connectionClass >
    private ReferenceFormatter formatter;
    private WrapperUtil util;
    </#if>

    @Override
    public void setContext(PluginContext ctx) {
        <#if interceptor??>
        _ctx = new WrapperContext(ctx, ${interceptor.canonicalName}.class);
        <#else>
        _ctx = new WrapperContext(ctx, null);
        </#if>
        <#if objectClass || connectionClass >
        BeanFactory beanFactory = _ctx.getPluginContext().getApplicationContext().getAutowireCapableBeanFactory();
        IPluginFactory factory = beanFactory.getBean(IPluginFactory.class);
        formatter = new ReferenceFormatter(factory);
        util = new WrapperUtil(_ctx, factory);
        </#if>
    }

    <#if findable>
    private Sid _internalId;

    public Sid getInternalId() {
        return _internalId;
    }

    public void setInternalId(Sid id) {
        this._internalId = id;
    }
    </#if>

    @Override
    public ${modelClass.canonicalName} __getTarget() {
        return (${modelClass.canonicalName}) _target;
    }

    public String getObjectClassName() {
        return __getTarget().getClass().getSimpleName();
    }

    <#list constructors as c> <#if !c.extensionClass??>
    <#if !(c.params?has_content)><#assign defaultConstructor = true></#if>
    <@compress single_line=true>public ${className}(<@params c />)<@thrown c /> {</@compress>
        setContext(AnonymousPluginContext.get());

        <@locals c />

        __setTarget(new ${c.declaringClass.canonicalName}(<@localNames c />));
    }
    </#if></#list>

    <#list constructors as c> <#if c.extensionClass??>
    <#if !(c.params?has_content)><#assign defaultConstructor = true></#if>
    <@compress single_line=true>public ${className}(<@params c />)<@thrown c /> {</@compress>
        setContext(AnonymousPluginContext.get());

        <@locals c />

        __setTarget(_ctx.constructWith(${c.extensionClass.canonicalName}.class).${c.extensionMethod}(<@localNames c />));
    }
    </#if></#list>

    <#if !defaultConstructor??>
    public ${className}() {
        // Empty default constructor
    }</#if>

    <#list methods as m> <#if !m.extensionClass?? && !interceptor?? && !m.inheritedWrapperMethod && (m.name != 'getDisplayName' || !objectClass)>
    <@compress single_line=true>public ${m.returns.returnFriendlyClassName} ${m.name}(<@params m />)<@thrown m /> {</@compress>
        <@locals m />

        <#if m.returns.returnFriendlyClassName != 'void'>
        Object _res$ = <#else>        </#if><#if m.originalName == 'setModel'>__getTarget().${m.originalName}((__getTarget().getModel()).getClass().cast(<@localNames m />));
        <#else>__getTarget().${m.originalName}(<@localNames m />);</#if>

        <#if m.returns.returnFriendlyClassName != 'void'>
        ${m.returns.returnFriendlyClassName} _res$pl = _ctx.createPluginObject(_res$, ${m.returns.convertFriendlyClassName});
        <#if findable>
        _ctx.assignId(${m.returns.convertFriendlyClassName}, _res$pl, getInternalId());
        <#elseif rootIdPropagated>
        _ctx.assignId(${m.returns.convertFriendlyClassName}, _res$pl, getRootId());
        </#if>
        <#if pluginName=='ConnectionManager' && m.returns.modelType.simpleName=='Connection'>
        Connection connection = (Connection) _res$;
        Connection_Wrapper wrapper = (Connection_Wrapper) _res$pl;
        wrapper.setInternalId(connection.getId());
        </#if>
        return _res$pl;
        </#if>
    }
    </#if></#list>

    <#list methods as m> <#if !m.extensionClass?? && interceptor??>
    <@compress single_line=true>public ${m.returns.returnFriendlyClassName} ${m.name} (<@params m />) throws Throwable {</@compress>
        <@locals m />

        <@compress single_line=true><#if m.returns.returnFriendlyClassName != 'void'>Object _res$ = <#else> </#if>
        _ctx.intercept(__getTarget(), <#if findable>getInternalId()<#else>null</#if>, "${m.originalName}", new Class<?>[] {<@types m />}<#if m.params?has_content>, new Object[]{<@localNames m />}</#if>);
        </@compress>

        <#if m.returns.returnFriendlyClassName != 'void'>
        ${m.returns.returnFriendlyClassName} _res$pl = _ctx.createPluginObject(_res$, ${m.returns.convertFriendlyClassName});
        <#if findable>
        _ctx.assignId(${m.returns.convertFriendlyClassName}, _res$pl, getInternalId());
        <#elseif rootIdPropagated>
        _ctx.assignId(${m.returns.convertFriendlyClassName}, _res$pl, getRootId());
        </#if>
        return _res$pl;
        </#if>
    }
    </#if></#list>

    <#list methods as m> <#if m.extensionClass??>
    <@compress single_line=true>public ${m.returns.returnFriendlyClassName} ${m.name} (<@params m />)<@thrown m /> {</@compress>
        <@locals m />
        <@compress single_line=true><#if m.returns.returnFriendlyClassName != 'void'>Object _res$ = <#else> </#if>
        _ctx.delegateTo(${m.extensionClass.canonicalName}.class, <#if findable>getInternalId()<#else>null</#if>, __getTarget()).${m.originalName}(<@localNames m />);
        </@compress>

        <#if m.returns.returnFriendlyClassName != 'void'>
        ${m.returns.returnFriendlyClassName} _res$pl = _ctx.createPluginObject(_res$, ${m.returns.convertFriendlyClassName});
        <#if findable>
        _ctx.assignId(${m.returns.convertFriendlyClassName}, _res$pl, getInternalId());
        <#elseif rootIdPropagated>
        _ctx.assignId(${m.returns.convertFriendlyClassName}, _res$pl, getRootId());
        </#if>
        return _res$pl;
        </#if>
    }
    </#if></#list>

    <#if singleton >
        <@singletonMethod />
    </#if>

    <#if objectClass || connectionClass>
    public Connection_Wrapper getConnection() {
    <#if connectionClass>
        <#-- This special case is here to provide consistent API for all findable objects. -->
        return this;
    <#else>
        return (Connection_Wrapper) util.findConnectionWrapper(getInternalId());
    </#if>
    }
    </#if>

    <#list executorMethods as m>
    <@compress single_line=true>public ${m.returns.typeName} ${m.name}(<@params m />)<@thrown m /> {</@compress>
        <@locals m />

        <@compress single_line=true><#if m.returns.returnFriendlyClassName != 'void'>${m.returns.fullClassName} _res$ = <#else> </#if>
        util.executor(getInternalId()).${m.name}(__getTarget()<#if m.params?has_content>,</#if><@localNames m />);
        </@compress>

        <#if m.returns.returnFriendlyClassName != 'void'>
        <#if m.returns.fullClassName != 'boolean'>
        if(_res$ == null) return null;
        </#if>

        ${m.returns.typeName} _res$pl = _ctx.createPluginObject(_res$, ${m.returns.convertFriendlyClassName});
        <#if m.returns.componentTypeName??>
        <#if m.returns.componentClassName??>
        for(int i = 0; i < _res$.size(); i++) {
            _res$pl.get(i).setInternalId(getInternalId().with("${m.returns.componentTypeName}",_res$.get(i).getUuid()));
        }
        <#else>
        _res$pl.setInternalId(getInternalId().with("${m.returns.componentTypeName}",_res$.getUuid()));
        </#if>
        </#if>
        return _res$pl;
        </#if>
    }

    </#list>

    <#if objectClass >
    <#list parents as parent>
    public void setParent${parent.simpleName}(${packageName}.${parent.simpleName}_Wrapper _parent) {

    <#if parent.simpleName != 'Connection'>
        __getTarget().setParent(_parent.__getTarget());
    </#if>

        String uuid = __getTarget().getUuid();
        Sid parentId = _parent.getInternalId();
        if (uuid == null)
            _internalId = parentId;
        else
            _internalId = parentId.with("${pluginName}", uuid);
    }
    </#list>

    public void create() {
        util.create(getInternalId(), __getTarget());
        // objects do not have valid uuid before create operation
        _internalId = getInternalId().with("${pluginName}", __getTarget().getUuid());
    }

    <#if draftClass >
    public void commitDrafts() {
        util.commitDrafts(getInternalId(), __getTarget());
    }

    public void discardDrafts() {
        util.discardDrafts(getInternalId(), __getTarget());
    }

    </#if>
    public void update() {
        util.update(getInternalId(), __getTarget());
    }

    public void read() {
        util.read(getInternalId(), __getTarget());
    }

    public void delete() {
        util.delete(getInternalId(), __getTarget());
    }

    <#list references as ref>
    //List returned by this method is read-only. Changes to the list will not be reflected in the state of the object.
    public java.util.List<${ref.className}_Wrapper> ${ref.pluginMethodName}() {
        return util.references(_internalId, ${ref.className}.class, __getTarget().${ref.methodName}());
    }

    </#list>

    <#list backrefs as bref>
    public boolean isReferencedBy${bref.className}(${bref.className}_Wrapper reference) {
        return ${bref.pluginMethodName}().stream().map(it -> it.getUuid()).collect(Collectors.toList()).contains(reference.getUuid());
    }
    </#list>

    public Integer backrefCount() {
        int count = 0;
        <#list backrefs as bref>
        count += ${bref.pluginMethodName}().size();
        </#list>
        return count;
    }

    <#list referenceProperties as prop>
    public String ${prop.wrapperMethodName}() {
        return formatter.format(this, __getTarget().${prop.methodName}(), "${prop.refObjectPluginType}");
    }
    </#list>

    public String getNonDraftParentType() {
        return util.nonDraftParentType(_internalId, __getTarget());
    }

    public String getNonDraftParentUuid() {
        return util.nonDraftParentUuid(_internalId, __getTarget());
    }
    </#if>

    <#list connectionFindClasses as clazz>
    public ${clazz.simpleName}_Wrapper find${clazz.pluginName}(String id) {
        return util.find(__getTarget(), ${clazz.simpleName}.class, id);
    }

    public ${clazz.simpleName}_Wrapper find${clazz.pluginName}ByFQName(String fqName) {
        return util.findByFQName(__getTarget(), ${clazz.simpleName}.class, fqName);
    }

    </#list>

    <#if nodeClass >
    public String getDisplayName() {
        return DisplayNameFormatter.INSTANCE.format(__getTarget());
    }
    </#if>


    <#list propertyViews as property>
    public String ${property.viewMethodName}() {
        ${property.propertyType} prop = __getTarget().${property.methodName}();
        if (prop != null) {
            return PropertyFormatter.INSTANCE.format(prop);
        } else {
            return null;
        }
    }

    </#list>
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        setContext(AnonymousPluginContext.get());
    }
}

<@compress single_line=true>
<#macro params m>
<#list m.params as p>
${p.returnFriendlyClassName} ${p.name}<#if p_has_next>, </#if>
</#list>
</#macro>
</@compress>

<@compress single_line=true>
<#macro locals m>
<#list m.params as p>
        ${p.modelType.canonicalName} _local$${p.name} = _ctx.extractModelObject(${p.name}, ${p.modelType.canonicalName}.class);
</#list>
</#macro>
</@compress>

<@compress single_line=true>
<#macro types m>
<#list m.params as p>
${p.modelType.canonicalName}.class<#if p_has_next>, </#if>
</#list>
</#macro>
</@compress>


<@compress single_line=true>
<#macro signature m>
<#list m.params as p>
${p.modelType.canonicalName}.class<#if p_has_next>, </#if>
</#list>
</#macro>
</@compress>

<@compress single_line=true>
<#macro localNames m>
<#list m.params as p>
_local$${p.name}<#if p_has_next>, </#if></#list></#macro>
</@compress>

<#macro singletonMethod>
    public static ${className} createScriptingSingleton(ch.dunes.vso.sdk.api.IPluginFactory f) {
        return ((AbstractModelDrivenFactory) f).createScriptingSingleton(${modelClass.canonicalName}.class);
    }
</#macro>

<#macro thrown c><#if c.thrown?has_content> throws <#list c.thrown as e>${e.canonicalName}<#if e_has_next>, <#else> </#if></#list><#else> </#if></#macro>