package org.springframework.beans.factory.support;

import org.apache.catalina.loader.WebappClassLoaderBase;
import org.apache.dubbo.common.utils.ConcurrentHashSet;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class MyLog {

    private static ThreadLocal<Integer> depth = new ThreadLocal<Integer>() {
        public Integer initialValue() {
            return 0;
        }
    };

    private static final String TAB_FLAG = "+--";

    static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    static boolean isSystemOut = false;

    static {
        if ("main".equals(Thread.currentThread().getName())) {
            isSystemOut = true;
        }
    }

    public static boolean isCacheType = true;
    public static boolean isLazyAll = false;
    public static boolean isCacheAop = true;

    static BufferedWriter out = null;

    static BufferedWriter beanMonitorOut = null;

    static {
        try {
            URL root = MyLog.class.getResource("/");
            File logRoot = new File(root.getPath()).getParentFile();
            logRoot.mkdirs();
            if (!isSystemOut) {
                File logFile = new File(logRoot.getPath() + File.separatorChar + "spring_startin2.log");

                System.out.println("%%%==:" + logFile);

//                out = new BufferedWriter(new FileWriter("d:\\longtime\\out\\out.txt"));
                out = new BufferedWriter(new FileWriter(logFile));
            }
            File logFile = new File(logRoot.getPath() + File.separatorChar + "spring_bean_monitor.log");
            System.out.println("%%%==:" + logFile);

//                out = new BufferedWriter(new FileWriter("d:\\longtime\\out\\out.txt"));
            beanMonitorOut = new BufferedWriter(new FileWriter(logFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {

        }
    }

    public static void addDepth() {
        depth.set(depth.get() + 1);

    }

    public static void decDepth() {
        depth.set(depth.get() - 1);
    }

    public static String getTabFlag() {
        int c = depth.get();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < c; i++) {
            sb.append(TAB_FLAG);
        }
        return sb.toString();
    }

    public static void logTree(String msg) {
        log("@TTT@:"+getTabFlag() + msg);
    }

    public static void log(String msg) {
        if (isSystemOut) {
            System.out.println(format.format(new Date()) + "--" + msg);
        } else {
            try {
                out.write(format.format(new Date()) + "--");
                out.write(msg);
                out.newLine();
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
            }
        }
    }

    public static void logMonitor(String msg) {
            try {
                beanMonitorOut.write(format.format(new Date()) + "--");
                beanMonitorOut.write(msg);
                beanMonitorOut.newLine();
                beanMonitorOut.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
            }
    }

    private static void logRaw(String msg) {
        {
            if (isSystemOut) {
                System.out.println(msg);
            } else {
                try {
                    out.write(msg);
                    out.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                }
            }
        }
    }

    public static void log(Throwable e) {
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        for (StackTraceElement item : stackTraceElements) {
            logRaw("    " + item.toString());
        }
        if (!isSystemOut) {
            try {
                out.flush();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } finally {
            }
        }
    }

    /**
     * Finds all the super types for the given {@link Class}.
     *
     * @param givenClazz input class
     * @return set of all super types that it inherits from
     */
    public static Set<Class<?>> getSuperTypes(Class<?> givenClazz) {
        final Set<Class<?>> superTypeSet = new LinkedHashSet<>();
        final Queue<Class<?>> possibleCandidates = new ArrayDeque<>();
        possibleCandidates.add(givenClazz);
        if (givenClazz.isInterface()) {
            possibleCandidates.add(Object.class);
        }
        while (!possibleCandidates.isEmpty()) {
            Class<?> clz = possibleCandidates.remove();
            // skip the input class as we are only interested in parent types
            if (!clz.equals(givenClazz)) {
                superTypeSet.add(clz);
            }
            Class<?> superClz = clz.getSuperclass();
            if (superClz != null) {
                possibleCandidates.add(superClz);
            }
            possibleCandidates.addAll(Arrays.asList(clz.getInterfaces()));
        }
        return superTypeSet;
    }

    public static Set<String> aopCacheSet = new ConcurrentHashSet<>();
    public static boolean hasAopCache = false;
    public static String start_speedup_dir = "start_speedup";
    public static String cacheAOP_names = "";

    public static boolean isNewCache = false;
    static{

        URL root = WebappClassLoaderBase.class.getResource("/");
        File cacheRoot = new File(root.getPath()).getParentFile();
        cacheRoot = new File(cacheRoot.getPath()+"/"+start_speedup_dir);

        cacheRoot.mkdirs();

        cacheAOP_names = cacheRoot.getPath()+"/cache_aop_names.txt";

        System.out.println("%%% load aop cache:" + cacheAOP_names);

        if (new File(cacheAOP_names).exists()) {
            hasAopCache = true;
            BufferedReader fileReader = null;
            try {
                fileReader = new BufferedReader(new FileReader(cacheAOP_names));
                String line = null;
                while ((line = fileReader.readLine()) != null) {
                    aopCacheSet.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e) {
                    }
                }
            }
        } else {
            isNewCache = true;
        }

        System.out.println("%%% load aop cache:" + cacheAOP_names);

    }

    public static void saveAopCache() {
        if(!isNewCache){
            return;
        }

        URL root = WebappClassLoaderBase.class.getResource("/");
        File cacheRoot = new File(root.getPath()).getParentFile();
        cacheRoot = new File(cacheRoot.getPath()+"/"+start_speedup_dir);

        cacheRoot.mkdirs();

        String cacheAOP_names = cacheRoot.getPath()+"/cache_aop_names.txt";

        System.out.println("%%% save aop cache:" + cacheAOP_names);

        BufferedWriter bufferedWriter =null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(cacheAOP_names));
            String[] names = aopCacheSet.toArray(new String[0]);
            for (String name : names) {
                bufferedWriter.write(name);
                bufferedWriter.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                }
            }
        }

        System.out.println("%%% save aop cache:" + aopCacheSet.size());
    }


    public static Set<String> doInitBeans = new HashSet<>();
    static{
        doInitBeans.add("cn.eteams.wechat.util.AdapterUtils");
        doInitBeans.add("com.weaver.base.common.web.biz.custombrowser.log.CustomBrowserLogger");
        doInitBeans.add("com.weaver.base.common.web.biz.custombrowser.util.RelevanceBrowserUtil");
        doInitBeans.add("com.weaver.teams.doc.e10.util.MessageQueueUtils");
        doInitBeans.add("com.weaver.teams.hrm.util.matrix.HrmMatrixDataUtil");
        doInitBeans.add("com.weaver.teams.hrm.util.matrix.HrmMatrixUtil");
        doInitBeans.add("com.weaver.inc.biz.util.DetachUtils");
        doInitBeans.add("com.weaver.meeting.util.MeetingEmployeeUtils");
        doInitBeans.add("com.weaver.mail.core.util.MailEmployeeUtils");
        doInitBeans.add("com.weaver.cowork.util.CoworkEmployeeUtils");
        doInitBeans.add("com.weaver.base.common.web.auth.util.PermissionCheckUtil");
        doInitBeans.add("com.weaver.inc.biz.util.PermissionUtils");
        doInitBeans.add("com.weaver.common.baseserver.info.BaseEnvInfoClient");
        doInitBeans.add("com.weaver.mail.core.util.MailModuleUtils");
        doInitBeans.add("com.weaver.inc.common.component.util.IncSystemUtils");
        doInitBeans.add("com.weaver.inc.proxy.util.SystemUtils");
        doInitBeans.add("com.weaver.common.baseserver.info.BaseEnvInfoUtils");
        doInitBeans.add("com.weaver.ebuilder.common.cache.EbuilderCacheUtil");
        doInitBeans.add("com.weaver.inc.common.component.cache.IncCacheTool");
        doInitBeans.add("com.weaver.attendweb.util.AttendProgressUtil");
        doInitBeans.add("com.weaver.scene.cache.SceneCacheUtil");
        doInitBeans.add("com.weaver.intlogin.common.IntLoginCommonUtil");
        doInitBeans.add("com.weaver.inc.data.util.HrmCacheUtils");
        doInitBeans.add("com.weaver.teams.hrm.util.batch.HrmBatchUtil");
        doInitBeans.add("com.weaver.inc.common.component.context.IncDepartmentContext");
        doInitBeans.add("com.weaver.common.hrm.util.HrmOrgUtil");
        doInitBeans.add("com.weaver.client.util.SecurityCasMethodUtils");
        doInitBeans.add("com.weaver.intunifyauth.client.cas.util.SecurityCasMethodUtils");
        doInitBeans.add("com.weaver.intunifyauth.client.oauth.util.SecurityCasMethodUtils");
        doInitBeans.add("com.weaver.server.util.SecurityCasMethodUtils");
        doInitBeans.add("com.weaver.intunifyauth.client.webseal.util.SecurityCasMethodUtils");
        doInitBeans.add("com.weaver.common.cas.client.common.SecurityCasUtils");
        doInitBeans.add("com.weaver.common.component.browser.handler.BrowserCustomHandler");
        doInitBeans.add("com.weaver.common.component.util.CommonPageUtil");
        doInitBeans.add("com.weaver.common.component.util.CommonSearchUtil");
        doInitBeans.add("com.weaver.common.component.util.AdvancedSearchHistoryTransformationUtil");
        doInitBeans.add("com.weaver.common.component.form.CardDetailForm");
        doInitBeans.add("com.weaver.customerservice.controller.form.FormController");
        doInitBeans.add("com.weaver.customerservice.manager.workflow.ApprovalFormService");
        doInitBeans.add("com.weaver.teams.hrm.util.HrmEmpEditUtil");
        doInitBeans.add("com.weaver.common.hrm.transformation.HrmEmpConvertUtil");
        doInitBeans.add("com.weaver.ebuilder.form.remote.util.ApiReqParamUtil");
        doInitBeans.add("com.weaver.esearch.data.core.schema.collector.feedback.FeedbackUtil");
        doInitBeans.add("com.weaver.esearch.data.core.schema.collector.hrm.HrmUtil");
        doInitBeans.add("com.weaver.teams.hrm.util.privacy.HrmPrivacyUtil");
        doInitBeans.add("com.weaver.common.hrm.util.privacy.HrmPrivacyUtil");
        doInitBeans.add("com.weaver.common.i18n.tool.config.date.format.DateTransformer");
        doInitBeans.add("com.weaver.ebuilder.form.base.parser.datetrans.DateTransConverter");
        doInitBeans.add("com.weaver.common.mybatis.crypto.handler.WeaBigDecimalTypeHandler");
        doInitBeans.add("com.weaver.common.mybatis.crypto.handler.WeaByteTypeHandler");
        doInitBeans.add("com.weaver.common.mybatis.crypto.handler.WeaLongTypeHandler");
        doInitBeans.add("com.weaver.common.mybatis.crypto.handler.WeaStringTypeHandler");
        doInitBeans.add("com.weaver.common.mybatis.crypto.handler.WeaShortTypeHandler");
        doInitBeans.add("com.weaver.common.mybatis.crypto.handler.WeaIntegerTypeHandler");
        doInitBeans.add("com.weaver.common.mybatis.crypto.handler.WeaFloatTypeHandler");
        doInitBeans.add("com.weaver.common.mybatis.crypto.handler.WeaDoubleTypeHandler");
        doInitBeans.add("com.weaver.common.mybatis.crypto.handler.WeaBooleanTypeHandler");
        doInitBeans.add("com.weaver.my.invoice.service.impl.InvoiceServiceImpl");
        doInitBeans.add("com.weaver.common.security.rpc.SecurityRpc");
        doInitBeans.add("com.weaver.common.ln.dao.LNRpc");
        doInitBeans.add("com.weaver.common.ln.util.LN");
        doInitBeans.add("com.weaver.common.security.rpc.RpcUtil");
        doInitBeans.add("com.weaver.common.serviceroute.ServiceRouteProperties");
        doInitBeans.add("com.weaver.esb.component.commonUtil.logger.service.impl.LoggerServiceImpl");
        doInitBeans.add("com.weaver.cusapp.utils.LogUtils");
        doInitBeans.add("com.weaver.customerservice.service.impl.BaseConfigServiceImpl");
        doInitBeans.add("com.weaver.customerservice.controller.order.OrderController");
        doInitBeans.add("com.weaver.customerservice.service.impl.WorkOrderCardServiceImpl");
        doInitBeans.add("com.weaver.customerservice.controller.order.OrderModuleController");
        doInitBeans.add("com.weaver.teams.hrm.util.encryptfield.HrmEncryptFieldUtil");
        doInitBeans.add("com.weaver.cowork.util.CoworkExtModuleUtils");
        doInitBeans.add("com.weaver.teams.hrapp.util.encryptfield.HrmEncryptFieldUtil");
        doInitBeans.add("org.apache.http.client.HttpClient");
        doInitBeans.add("org.springframework.http.client.ClientHttpRequestFactory");
        doInitBeans.add("org.springframework.web.client.RestTemplate");
        doInitBeans.add("com.weaver.eb.common.component.httpclient.HttpClientAutoConfiguration");
        doInitBeans.add("com.weaver.eb.crm.util.ExternalContactUtils");
        doInitBeans.add("com.weaver.intcenter.hr.controller.TestController");
        doInitBeans.add("com.weaver.ebuilder.datasource.conn.ConnectionPool");
        doInitBeans.add("com.weaver.ebuilder.datasource.util.ExternalDSUtil");
        doInitBeans.add("com.weaver.ebuilder.form.base.parser.condition.handler.CommonConditionHandler");
        doInitBeans.add("com.weaver.ebuilder.form.base.utils.physicaltable.condition.handler.CommonConditionHandler");
        doInitBeans.add("com.weaver.edc.common.report.analysis.configuration.ApplicationContextConfig");
        doInitBeans.add("com.weaver.edc.common.report.analysis.service.impl.SheetServiceImpl");
        doInitBeans.add("com.weaver.edc.common.report.analysis.service.impl.EdcReportDevDesignServiceImpl");
        doInitBeans.add("com.weaver.my.base.utils.HttpUtil");
        doInitBeans.add("com.weaver.esb.server.core.impl.DefaultEsbEngine");
        doInitBeans.add("com.weaver.escheduler.admin.core.conf.ESchedulerAdminConfig");
        doInitBeans.add("com.weaver.esearch.data.controller.ESDModuleDiySetController");
        doInitBeans.add("com.weaver.esearch.search.controller.ESModuleSetController");
        doInitBeans.add("com.weaver.esearch.search.util.ModuleUtil");
        doInitBeans.add("com.weaver.esearch.search.service.db.impl.ESModuleSetServiceImpl");
        doInitBeans.add("com.weaver.eteams.file.client.utils.FileUtils");
        doInitBeans.add("com.weaver.mail.core.weavermail.send.EmailSendUtils");
        doInitBeans.add("com.weaver.batch.util.FromExcelUtils");
        doInitBeans.add("com.weaver.excel.formula.entity.parameter.ExcelFuncs");
        doInitBeans.add("com.weaver.file.online.yozo.middleground.utils.YozoUtils");
        doInitBeans.add("com.weaver.fna.expense.common.action.FexsBorrowEffectAction");
        doInitBeans.add("com.weaver.fna.expense.common.action.FexsBorrowRepayFreeAction");
        doInitBeans.add("com.weaver.fna.expense.common.action.FexsExpenseEffectAction");
        doInitBeans.add("com.weaver.fna.expense.common.action.FexsExpenseFreeAction");
        doInitBeans.add("com.weaver.fna.expense.common.action.FexsExpenseFreezeAction");
        doInitBeans.add("com.weaver.fna.expense.common.action.FexsRepayEffectAction");
        doInitBeans.add("com.weaver.fna.expense.common.action.FexsRepayFreezeAction");
        doInitBeans.add("com.weaver.fna.expense.common.validation.ConfigBuilder");
        doInitBeans.add("com.weaver.fna.expense.common.validation.FormDataBuilder");
        doInitBeans.add("com.weaver.fna.expense.common.validation.PeriodDataBuilder");
        doInitBeans.add("com.weaver.fna.expense.util.AmountUtil");
        doInitBeans.add("com.weaver.fna.expense.util.base.FormSetProvider");
        doInitBeans.add("com.weaver.fna.expense.util.base.OptionsProvider");
        doInitBeans.add("com.weaver.fna.expense.util.base.SubjectProvider");
        doInitBeans.add("com.weaver.fna.expense.util.budget.ImportUtil");
        doInitBeans.add("com.weaver.fna.expense.util.CustomHomePageUtil");
        doInitBeans.add("com.weaver.fna.expense.util.ESearchUtil");
        doInitBeans.add("com.weaver.fna.expense.util.FexsDefaultWfUtil");
        doInitBeans.add("com.weaver.fna.expense.util.FexsExpenseInfoUtil");
        doInitBeans.add("com.weaver.fna.expense.util.FexsFormMergePcEmUtil");
        doInitBeans.add("com.weaver.fna.expense.util.FexsFormSettingUtil");
        doInitBeans.add("com.weaver.fna.expense.util.FexsInvoiceInfoUtil");
        doInitBeans.add("com.weaver.fna.expense.util.FexsLeftMenuUtil");
        doInitBeans.add("com.weaver.fna.expense.util.FexsUtil");
        doInitBeans.add("com.weaver.fna.expense.util.FnaActionUtil");
        doInitBeans.add("com.weaver.fna.expense.util.FormPageShowUtil");
        doInitBeans.add("com.weaver.fna.expense.util.LogUtil");
        doInitBeans.add("com.weaver.fna.expense.util.OptionsettingUtil");
        doInitBeans.add("com.weaver.fna.expense.util.permission.PermissionUtil");
        doInitBeans.add("com.weaver.fna.expense.util.processFlow.ProcessFlowUtil");
        doInitBeans.add("com.weaver.fna.expense.util.RecentFeedbackUtil");
        doInitBeans.add("com.weaver.fna.expense.util.RecycleBinUtil");
        doInitBeans.add("com.weaver.fna.expense.util.StandardUtil");
        doInitBeans.add("com.weaver.fna.expense.util.subject.SubjectUtil");
        doInitBeans.add("com.weaver.framework.limit.WeaverLimitFilterBean");
        doInitBeans.add("com.weaver.inc.biz.util.BizUtils");
        doInitBeans.add("com.weaver.inc.biz.util.ExtUtils");
        doInitBeans.add("com.weaver.inc.biz.service.impl.InvoiceInfoSyncServiceImpl");
        doInitBeans.add("com.weaver.inc.biz.util.BsonUtils");
        doInitBeans.add("com.weaver.inc.biz.util.LianCheckUtils");
        doInitBeans.add("com.weaver.inc.biz.util.UseSettingCheckUtils");
        doInitBeans.add("com.weaver.inc.biz.util.OverdueCheckUtils");
        doInitBeans.add("com.weaver.inc.biz.util.BizCheckUtils");
        doInitBeans.add("com.weaver.inc.biz.util.OcrValidUtils");
        doInitBeans.add("com.weaver.inc.biz.util.IncYyBizUtils");
        doInitBeans.add("com.weaver.inc.common.util.IncAdapterUtils");
        doInitBeans.add("com.weaver.inc.biz.util.LoginUserUtils");
        doInitBeans.add("com.weaver.inc.common.util.IncAsyncUtils");
        doInitBeans.add("com.weaver.inc.common.util.IncBizUtils");
        doInitBeans.add("com.weaver.inc.common.util.IncParserUtils");
        doInitBeans.add("com.weaver.inc.common.component.util.MsgRestUtils");
        doInitBeans.add("com.weaver.inc.biz.api.util.MessageUtils");
        doInitBeans.add("com.weaver.inc.biz.api.util.UserUtils");
        doInitBeans.add("com.weaver.inc.proxy.util.ProxyUtils");
        doInitBeans.add("com.weaver.inc.data.util.CacheUtils");
        doInitBeans.add("com.weaver.inc.data.util.CorpUtils");
        doInitBeans.add("com.weaver.inc.mail.util.SmsOrcUtils");
        doInitBeans.add("com.weaver.intcenter.mail.util.MailListData");
        doInitBeans.add("com.weaver.intlogin.common.constant.ParamsRuleConstant");
        doInitBeans.add("com.weaver.mail.base.common.FormOptions");
        doInitBeans.add("com.weaver.meeting.util.IMMessageUtil");
        doInitBeans.add("com.weaver.meeting.util.video.VideoMeetingClient");
        doInitBeans.add("com.weaver.meeting.util.video.VideoMeetingUtil");
        doInitBeans.add("com.weaver.odoc.browser.applicationsetting.OdocMenuBrowser");
        doInitBeans.add("com.weaver.odoc.service.impl.applicationsetting.OdocListFieldServiceImpl");
        doInitBeans.add("com.weaver.odoc.service.impl.custompage.OdocCustomMenuServiceImpl");
        doInitBeans.add("com.weaver.signcenter.util.QYSUserUtils");
        doInitBeans.add("com.weaver.statistics.utils.LogUtils");
        doInitBeans.add("com.weaver.esb.setting.common.util.EsbUtil");
        doInitBeans.add("com.weaver.teams.basic.utils.BasicCommonUtils");
        doInitBeans.add("com.weaver.eb.gateway.util.GatewayUtils");
        doInitBeans.add("com.weaver.eb.client.oauth.OauthUtils");
        doInitBeans.add("com.weaver.teams.utils.CacheUtils");
        doInitBeans.add("com.weaver.teams.crm.common.entity.CrmSystemFieldMapper");
        doInitBeans.add("com.weaver.teams.crm.extend.job.JobServiceImpl");
        doInitBeans.add("com.weaver.teams.elog.service.BaseCommonElogWriter");
        doInitBeans.add("com.weaver.teams.hrm.util.topmenu.HrmTopMenuUtil");
        doInitBeans.add("com.weaver.teams.hrm.util.offspace.HrmOffspaceBrowserUtil");
        doInitBeans.add("com.weaver.teams.cloudSms.CloudSmsSend");
        doInitBeans.add("com.weaver.tenant.service.tenant.common.TenantCommonSelectServiceImpl");
        doInitBeans.add("com.weaver.tenant.manager.tencentcloud.TencentCloudRemoteController");
        doInitBeans.add("com.weaver.workflow.engine.formdef.config.FormManagerConfig");
        doInitBeans.add("com.weaver.workflow.mark.utils.MarkModuleUtil");
        doInitBeans.add("com.weaver.workflow.common.util.ModuleUtil");
        doInitBeans.add("com.weaver.workflow.common.util.DateTimeUtil");
        doInitBeans.add("com.weaver.workflow.core.util.flow.AgentUtil");
        doInitBeans.add("com.weaver.workflow.core.util.flow.RequestDataUtil");
        doInitBeans.add("com.weaver.workflow.core.util.flow.WorkflowOvertimeUtil");
        doInitBeans.add("com.weaver.workflow.core.util.module.WfcModuleUtil");
        doInitBeans.add("com.weaver.workflow.core.util.task.WfcTaskUtil");
        doInitBeans.add("com.weaver.workflow.list.base.util.RequestListUtil");
        doInitBeans.add("com.weaver.workflow.list.builddata.util.RequestListBuildDataUtil");
        doInitBeans.add("com.weaver.workflow.list.controller.E9RequestListDataAction");
        doInitBeans.add("com.weaver.workflow.list.customset.util.CustomSetUtil");
        doInitBeans.add("com.weaver.workflow.list.customset.util.RequestListButtonUtil");
        doInitBeans.add("com.weaver.workflow.list.customset.util.RequestListDimensionUtil");
        doInitBeans.add("com.weaver.workflow.list.job.remind.RequestTodoRemindCache");
        doInitBeans.add("com.weaver.workflow.list.job.remind.RequestTodoRemindUtil");
        doInitBeans.add("com.weaver.workflow.list.requestname.job.AsyncParseRequestTitleManager");
        doInitBeans.add("com.weaver.workflow.list.requestname.util.RequestTitleParseUtil");
        doInitBeans.add("com.weaver.workflow.list.api.rest.impl.publicApi.WflRequestListRpc");
        doInitBeans.add("com.weaver.workflow.list.controller.E9ListPublicAPIAction");
        doInitBeans.add("com.weaver.workflow.list.util.center.RequestListCenterUtil");
        doInitBeans.add("com.weaver.workflow.list.util.CustomQuerySet.CustomQuerySetUtil");
        doInitBeans.add("com.weaver.workflow.list.util.factory.RequestListQueryWrapperFactory");
        doInitBeans.add("com.weaver.workflow.list.util.newflow.RequestListNewFlowUtil");
        doInitBeans.add("com.weaver.workflow.list.util.ofs.OfsDataUtil");
        doInitBeans.add("com.weaver.workflow.list.util.portal.PortalRequestListUtil");
        doInitBeans.add("com.weaver.workflow.list.util.RequestListAttentionUtil");
        doInitBeans.add("com.weaver.workflow.list.util.RequestListColumnUtil");
        doInitBeans.add("com.weaver.workflow.list.util.RequestListConvertUtil");
        doInitBeans.add("com.weaver.workflow.list.util.RequestListDataUtil");
        doInitBeans.add("com.weaver.workflow.list.util.RequestListDetachUtil");
        doInitBeans.add("com.weaver.workflow.list.util.RequestListOperateCheckUtil");
        doInitBeans.add("com.weaver.workflow.list.util.RequestListOrderUtil");
        doInitBeans.add("com.weaver.workflow.list.util.RequestListPermissionUtil");
        doInitBeans.add("com.weaver.workflow.list.util.RequestListPurchaseModuleUtil");
        doInitBeans.add("com.weaver.workflow.list.util.RequestListSearchAdUtil");
        doInitBeans.add("com.weaver.workflow.list.util.RequestListSearchParamsUtil");
        doInitBeans.add("com.weaver.workflow.list.util.RequestListSecLevelUtil");
        doInitBeans.add("com.weaver.workflow.list.util.smartprocess.SmartProcessUtil");
        doInitBeans.add("com.weaver.workflow.list.util.supervise.SuperviseUtil");
        doInitBeans.add("com.weaver.workflow.menu.util.FlowMenuUtil");
        doInitBeans.add("com.weaver.workflow.pathdef.global.util.multi.MultiDataUtilNew");
        doInitBeans.add("com.weaver.workflow.pathdef.global.util.WfBrowserDataUtil");
        doInitBeans.add("com.weaver.workflow.report.appformstat.util.ImportUtils");
        doInitBeans.add("com.weaver.workflow.report.appformstat.util.ReportConditionUtil");
        doInitBeans.add("com.weaver.workflow.report.appformstat.util.ReportTableUtil");
        doInitBeans.add("com.weaver.workflow.report.effreport.util.WfrDetachUtil");
        doInitBeans.add("com.weaver.workflow.report.pubutil.ExportReportUtil");
        doInitBeans.add("com.weaver.workflow.report.pubutil.RequestReportPurchaseModuleUtil");
        doInitBeans.add("com.weaver.workflow.report.pubutil.SecLevelReportUtil");
        doInitBeans.add("com.weaver.workflow.report.pubutil.WfrAnalyseReportUtil");
        doInitBeans.add("com.weaver.workflow.report.pubutil.WfrCustomReportUtil");
        doInitBeans.add("com.weaver.workflow.rule.condition.factory.ConditionServiceFactory4Workflow");
        doInitBeans.add("com.weaver.workflow.unoperator.util.UnOperatorUtil");
        doInitBeans.add("com.weaver.common.security.util.SpringUtils");
        doInitBeans.add("com.weaver.common.elog.service.ApplicationContextProvider");
        doInitBeans.add("com.weaver.framework.context.WeaverSpringContext");
        doInitBeans.add("com.weaver.datasecurity.config.DataSecurityApplicationContext");
        doInitBeans.add("com.weaver.teams.core.orm.OrmSpringContext");
        doInitBeans.add("com.weaver.esb.base.utils.EsbApplicationContextProvider");
        doInitBeans.add("com.weaver.ebuilder.common.spring.EbuilderSpringUtils");
        doInitBeans.add("com.weaver.utils.SentinelSpringContextHolder");
        doInitBeans.add("com.weaver.component.base.util.SpringContext");
        doInitBeans.add("com.weaver.common.distribution.lock.util.SpringUtilLock");
        doInitBeans.add("com.weaver.common.security.util.CryptoSpringContextHolder");
        doInitBeans.add("com.weaver.common.form.physical.util.SpringBeanUtils");
        doInitBeans.add("com.weaver.ebuilder.datasource.api.util.DataSourceSpringContext");
        doInitBeans.add("com.weaver.common.component.browser.register.WeaBrowserRegister");
        doInitBeans.add("com.weaver.common.i18n.tool.util.I18nContextUtil");
        doInitBeans.add("com.weaver.common.cache.laycache.util.CacheSpringContextHolder");
        doInitBeans.add("com.weaver.teams.util.ApplicationContextUtils");
        doInitBeans.add("com.weaver.common.component.browser.util.WeaverCommonBeanUtil");
        doInitBeans.add("com.weaver.common.async.util.ApplicationContextUtil");
        doInitBeans.add("cn.eteams.wechat.user.SpringApplicationContext");
        doInitBeans.add("com.weaver.common.form.stat.register.dataStat.DataStatRegister");
        doInitBeans.add("com.weaver.common.mybatis.monitor.interceptor.MonitorSqlContextHolder");
        doInitBeans.add("com.weaver.common.hrm.util.HrmContextUtil");
        doInitBeans.add("com.weaver.ebuilder.form.common.utils.service.ApplicationContextHelper");
        doInitBeans.add("com.weaver.attendweb.framework.util.WorkflowSpringUtil");
        doInitBeans.add("com.weaver.teams.datarule.utils.SpringContextUtil");
        doInitBeans.add("com.weaver.teams.blog.util.SpringContextUtils");
        doInitBeans.add("com.weaver.workflow.common.framework.util.WorkflowSpringUtil");
        doInitBeans.add("com.weaver.workflow.common.hook.util.WfListDataExtUtil");
        doInitBeans.add("com.weaver.workflow.common.hook.util.WfDataExtendUtil");
        doInitBeans.add("com.weaver.workflow.common.hook.util.WfCommonHookUtil");
        doInitBeans.add("com.weaver.teams.doc.e10.config.SpringContextUtils");
        doInitBeans.add("com.weaver.workflow.engine.version.util.WfCnvSpecialHandlerUtil");
        doInitBeans.add("com.weaver.workflow.engine.maintain.util.WfpBatchMaintainUtil");
        doInitBeans.add("com.weaver.workflow.engine.version.util.WfCnvFormHandlerUtil");
        doInitBeans.add("com.weaver.cowork.util.CoworkSpringContextUtil");
        doInitBeans.add("com.weaver.eb.common.component.context.SpringApplicationContext");
        doInitBeans.add("com.weaver.ecode.util.ApplicationContextUtil");
        doInitBeans.add("com.weaver.edc.common.report.analysis.provider.ReportApplicationContextProvider");
        doInitBeans.add("com.weaver.esearch.data.core.context.SpringApplicationContext");
        doInitBeans.add("com.weaver.esearch.search.core.context.SpringApplicationContext");
        doInitBeans.add("com.weaver.excel.formula.init.ExcelSpringContext");
        doInitBeans.add("com.weaver.file.online.common.util.ApplicationContextBeanUtils");
        doInitBeans.add("com.weaver.front.monitor.util.common.SpringBeanUtil");
        doInitBeans.add("com.weaver.intcenter.hr.util.SpringContextUtil");
        doInitBeans.add("com.weaver.intcenter.ldap.util.SpringContextUtil");
        doInitBeans.add("com.weaver.intlogin.common.until.SpringUtil");
        doInitBeans.add("com.weaver.client.util.SpringContextBeansUtil");
        doInitBeans.add("com.weaver.intunifyauth.client.cas.util.SpringContextBeansUtil");
        doInitBeans.add("com.weaver.intunifyauth.client.oauth.util.SpringContextBeansUtil");
        doInitBeans.add("com.weaver.server.util.SpringContextBeansUtil");
        doInitBeans.add("com.weaver.mail.base.datatransform.mailinfo.MailBaseSpringBeanUtil");
        doInitBeans.add("com.weaver.mail.core.util.MailCoreSpringBeanUtil");
        doInitBeans.add("com.weaver.mail.receive.util.MailReceiveSpringBeanUtil");
        doInitBeans.add("com.weaver.meeting.core.chain.util.MtSpringContextUtil");
        doInitBeans.add("com.weaver.passport.config.ProfileConfig");
        doInitBeans.add("com.weaver.sms.component.SmsApplicationContextProvider");
        doInitBeans.add("com.weaver.signcenter.util.qys.SpringContextUtils");
        doInitBeans.add("com.weaver.tenant.base.SpringContext");
        doInitBeans.add("com.weaver.tenant.utils.ProfileConfig");
        doInitBeans.add("com.weaver.systemeimodule.util.SystemeiSpringBeanUtil");
        doInitBeans.add("com.weaver.intcenter.mail.util.SpringContextUtil");
        doInitBeans.add("com.weaver.odoc.sdks.workflowsdk.hook.version.util.OdocWfNewVersionHandlerUtil");
        doInitBeans.add("com.weaver.common.form.context.SpringContext");
        doInitBeans.add("com.weaver.esb.setting.common.util.EsbServiceInjectUtil");
        doInitBeans.add("com.weaver.esb.component.base.bean.ComponentFactory");
        doInitBeans.add("com.weaver.esb.setting.design.controller.DesignController");
        doInitBeans.add("com.weaver.dw.datamodel.base.mvc.SpringContext");
        doInitBeans.add("com.weaver.emonitor.util.MonitorSpringContextUtil");
        doInitBeans.add("com.weaver.common.baseserver.context.BaseServerSpringContextHolder");
        doInitBeans.add("com.weaver.workflow.common.util.SpringBeanUtil");
        doInitBeans.add("com.weaver.workflow.engine.version.util.WfCnvHookHandlerUtil");
        doInitBeans.add("com.weaver.em.msg.componet.register.MsgTempRegister");
        doInitBeans.add("com.weaver.intunifyauth.client.webseal.util.SpringContextBeansUtil");
        doInitBeans.add("com.weaver.ebuilder.form.base.utils.SpringConfigTool");
        doInitBeans.add("com.weaver.common.form.base.FormBizPostProcessor");
        doInitBeans.add("com.weaver.dw.platform.bimodel.util.SpringContext");
        doInitBeans.add("com.weaver.loom.context.cache.BaseHolder");
        doInitBeans.add("com.weaver.teams.crm.permission.PermissionFilter");
        doInitBeans.add("com.weaver.ebuilder.form.common.utils.splittable.Util_TableMap");
        doInitBeans.add("com.weaver.teams.email.EmailServiceImpl");
        doInitBeans.add("com.weaver.teams.sms.SmsServiceImpl");
        doInitBeans.add("com.weaver.blog.impl.RemoteWorkResultLoginServiceImpl");
        doInitBeans.add("com.weaver.teams.salary.service.email.SalaryEmailServiceImpl");
        doInitBeans.add("com.weaver.teams.rest.SalarybillController");
        doInitBeans.add("com.weaver.teams.salary.controller.SalaryController");
        doInitBeans.add("com.weaver.calendar.controller.CalendarCommonShareController");
        doInitBeans.add("com.weaver.intcenter.hr.util.PropertiesUtils");
        doInitBeans.add("com.weaver.edc.common.dataset.utils.ServiceUtil");
        doInitBeans.add("com.weaver.inc.common.http.HttpUtils");
        doInitBeans.add("com.weaver.inc.common.component.util.IncYyUtils");
        doInitBeans.add("com.weaver.inc.common.component.util.UploadFileUtils");

    }

    public static Set<String> cacheSet = new HashSet<>();
    static{
//        cacheSet.add("org.apache.dubbo.config.ApplicationConfig");
//        cacheSet.add("org.apache.dubbo.config.ConsumerConfig");
//        cacheSet.add("com.weaver.teams.cache.MemcacheClient");
//        cacheSet.add("org.apache.ibatis.session.SqlSessionFactory");
//        cacheSet.add("org.mybatis.spring.SqlSessionTemplate");
//        cacheSet.add("org.springframework.boot.context.properties.ConfigurationPropertiesBindHandlerAdvisor");
//        cacheSet.add("org.apache.dubbo.config.RegistryConfig");
//        cacheSet.add("org.apache.dubbo.config.ProviderConfig");
//        cacheSet.add("org.apache.dubbo.config.ProtocolConfig");
//        cacheSet.add("org.springframework.context.ApplicationContext");
//        cacheSet.add("com.weaver.teams.remote.eteams.EteamsRemotingService");
//        cacheSet.add("com.weaver.common.cas.client.WeaverCasUserDetailService");
//        cacheSet.add("com.baomidou.mybatisplus.core.mapper.BaseMapper");
//        cacheSet.add("org.springframework.integration.config.annotation.Disposables");
//        cacheSet.add("org.springframework.beans.factory.BeanFactory");
//        cacheSet.add("com.weaver.teams.cache.memcached.TeamsMemcacheClient");
//        cacheSet.add("com.weaver.common.cache.base.BaseCache");
//        cacheSet.add("org.springframework.boot.actuate.health.HealthAggregator");
//        cacheSet.add("com.weaver.common.cas.client.properties.WeaverCasProperties");
//        cacheSet.add("com.weaver.teams.client.fetion.app.FxAppService");
//        cacheSet.add("com.weaver.common.cas.client.cache.SecurityContextCache");
//        cacheSet.add("org.springframework.core.env.Environment");
//        cacheSet.add("com.weaver.common.cache.tablecache.impl.ComInfoCache");
//        cacheSet.add("com.weaver.ebuilder.datasource.api.util.SqlGenerateUtil");
//        cacheSet.add("com.weaver.common.async.producer.client.AsyncClient");
//        cacheSet.add("com.weaver.teams.core.entity.IdGenerator");
//        //下面是占比最大的
        cacheSet.add("org.apache.dubbo.config.ApplicationConfig");
        cacheSet.add("org.apache.dubbo.config.ModuleConfig");
        cacheSet.add("org.apache.dubbo.config.RegistryConfig");
        cacheSet.add("org.apache.dubbo.config.ProtocolConfig");
        cacheSet.add("org.apache.dubbo.config.MonitorConfig");
        cacheSet.add("org.apache.dubbo.config.ProviderConfig");
        cacheSet.add("org.apache.dubbo.config.ConsumerConfig");
        cacheSet.add("org.apache.dubbo.config.spring.ConfigCenterBean");
        cacheSet.add("org.apache.dubbo.config.MetadataReportConfig");
        cacheSet.add("org.apache.dubbo.config.MetricsConfig");
        cacheSet.add("org.apache.dubbo.config.SslConfig");
    }

    public static Set<String> noGoodVars = new HashSet<>();

    static{
        noGoodVars.add("com.weaver.teams.attend.app.AttendAction");
        noGoodVars.add("com.weaver.sms.service.impl.SmsServiceImpl");
        noGoodVars.add("com.weaver.sms.service.impl.SmsServiceImplSenderCl");
        noGoodVars.add("com.weaver.sms.service.impl.SmsServiceImplSenderClMarket");
        noGoodVars.add("com.weaver.sms.service.impl.SmsServiceImplSenderSelf");
        noGoodVars.add("com.weaver.sms.service.impl.SmsServiceImplSenderYd");
        noGoodVars.add("com.weaver.sms.service.impl.SmsServiceImplSenderYm");
        noGoodVars.add("com.weaver.sms.service.impl.SmsServiceImplSenderYmMarket");
        noGoodVars.add("com.weaver.workflow.core.RpcInitContents");
        noGoodVars.add("com.weaver.teams.attend.service.AttendAppealServiceImpl");
        noGoodVars.add("com.weaver.attendweb.async.AttendFormulaProcessorListener1");
        noGoodVars.add("com.weaver.teams.attend.service.AttendRemindDataServiceImpl");
        noGoodVars.add("com.weaver.attendweb.service.Attend4MobileServiceImpl");
        noGoodVars.add("cn.eteams.wechat.servicenumber.ServiceNumberInfo");
        noGoodVars.add("com.weaver.architecture.controller.E10McController");
        noGoodVars.add("com.weaver.attendweb.async.AttendAutoSignAsyncListener");
        noGoodVars.add("com.weaver.attendweb.controller.AttendInnerRestController");
        noGoodVars.add("com.weaver.attendweb.controller.AttendVacationTypesController");
        noGoodVars.add("com.weaver.attendweb.controller.outinterface.Attend4MobileController");
        noGoodVars.add("com.weaver.attendweb.trigger.AttendPushAbnormalRemindDataScheduler");
        noGoodVars.add("com.weaver.attendweb.trigger.AttendPushSubscriptionScheduler");
        noGoodVars.add("com.weaver.attendweb.trigger.AttendStatusDetailQuartzScheduler");
        noGoodVars.add("com.weaver.attendweb.trigger.CheckNonNormalFLowDataScheduler");
        noGoodVars.add("com.weaver.base.common.web.controller.pc.ConfigurationController");
        noGoodVars.add("com.weaver.base.common.web.controller.pc.RemindController");
        noGoodVars.add("com.weaver.base.common.web.controller.pc.UrgeRemindController");
        noGoodVars.add("com.weaver.base.common.web.util.CustomBrowserUtil");
        noGoodVars.add("com.weaver.batch.service.impl.BatchExportServiceImpl");
        noGoodVars.add("com.weaver.batch.service.impl.BatchImportServiceImpl");
        noGoodVars.add("com.weaver.blog.controller.app.MBlogMessageController");
        noGoodVars.add("com.weaver.blog.controller.web.BlogMessageController");
        noGoodVars.add("com.weaver.blog.service.impl.BlogShareServiceImpl");
        noGoodVars.add("com.weaver.common.authority.service.SanYuanManager");
        noGoodVars.add("com.weaver.common.cache.laycache.config.LayeringCacheAutoConfig");
        noGoodVars.add("com.weaver.common.form.metadata.complex.ComplexFieldServiceImpl");
        noGoodVars.add("com.weaver.common.form.metadata.field.FormFieldServiceImpl");
        noGoodVars.add("com.weaver.crm.market.resource.service.impl.ResourceServiceImpl");
        noGoodVars.add("com.weaver.eb.base.controller.BaseSysInfoController");
        noGoodVars.add("com.weaver.eb.base.service.impl.BaseSysInfoSerivceImpl");
        noGoodVars.add("com.weaver.eb.common.component.log.EbSyncLogCustom");
        noGoodVars.add("com.weaver.ebuilder.flow.utils.msg.ApprovalSendMsgUtil");
        noGoodVars.add("com.weaver.ebuilder.form.action.paitem.util.TableNameUtils");
        noGoodVars.add("com.weaver.ebuilder.form.base.utils.physicaltable.PhysicalTableUtil");
        noGoodVars.add("com.weaver.ebuilder.form.base.utils.right.RightUtil");
        noGoodVars.add("com.weaver.ebuilder.form.controller.us.TestDubboController");
        noGoodVars.add("com.weaver.ebuilder.form.view.list.service.impl.ShareDataServiceImpl");
        noGoodVars.add("com.weaver.ecode.common.helper.EcodeJavaFolderHelper");
        noGoodVars.add("com.weaver.ecode.common.helper.EcodeSendMsgHelper");
        noGoodVars.add("com.weaver.esb.component.attend.GetAttendInfoComponent");
        noGoodVars.add("com.weaver.esb.component.crm.common.CrmRemindComponent");
        noGoodVars.add("com.weaver.esb.component.crm.contract.ContractInvoiceCreateComponent");
        noGoodVars.add("com.weaver.esb.component.crm.contract.ContractInvoiceUpdateComponent");
        noGoodVars.add("com.weaver.esb.component.crm.contract.ContractPayCreateComponent");
        noGoodVars.add("com.weaver.esb.component.crm.contract.ContractPayUpdateComponent");
        noGoodVars.add("com.weaver.esb.component.crm.contract.ContractReceiveCreateComponent");
        noGoodVars.add("com.weaver.esb.component.crm.contract.ContractReceiveUpdateComponent");
        noGoodVars.add("com.weaver.esb.component.crm.contract.CrmContractCreateComponent");
        noGoodVars.add("com.weaver.esb.component.crm.contract.CrmContractDeleteComponent");
        noGoodVars.add("com.weaver.esb.component.crm.contract.CrmContractNullifyComponent");
        noGoodVars.add("com.weaver.esb.component.crm.contract.CrmContractUpdateComponent");
        noGoodVars.add("com.weaver.esb.component.crm.customer.CrmCustomerCreateComponent");
        noGoodVars.add("com.weaver.esb.component.crm.customer.CrmCustomerDeleteComponent");
        noGoodVars.add("com.weaver.esb.component.crm.customer.CrmCustomerReceiveComponent");
        noGoodVars.add("com.weaver.esb.component.crm.customer.CrmCustomerReleaseComponent");
        noGoodVars.add("com.weaver.esb.component.crm.customer.CrmCustomerUpdateComponent");
        noGoodVars.add("com.weaver.esb.component.crm.market.CrmMarketActivityCreateComponent");
        noGoodVars.add("com.weaver.esb.component.crm.market.CrmMarketActivityDeleteComponent");
        noGoodVars.add("com.weaver.esb.component.crm.market.CrmMarketActivityRestoreComponent");
        noGoodVars.add("com.weaver.esb.component.crm.market.CrmMarketActivityUpdateComponent");
        noGoodVars.add("com.weaver.esb.component.crm.orderform.CrmOrderFormCreateComponent");
        noGoodVars.add("com.weaver.esb.component.crm.orderform.CrmOrderFormDeleteComponent");
        noGoodVars.add("com.weaver.esb.component.crm.orderform.CrmOrderFormNullifyComponent");
        noGoodVars.add("com.weaver.esb.component.crm.orderform.CrmOrderFormUpdateComponent");
        noGoodVars.add("com.weaver.esb.component.em.EmSaveServerInfoComponent");
        noGoodVars.add("com.weaver.esb.component.hrm.HrmWorkConditionChangeComponent");
        noGoodVars.add("com.weaver.esb.component.invoice.CashBookReimComponent");
        noGoodVars.add("com.weaver.esb.component.invoice.InvoiceReimComponent");
        noGoodVars.add("com.weaver.esb.component.open.OpenCallbackComponent");
        noGoodVars.add("com.weaver.esb.component.sms.report.SmsSendReportComponent");
        noGoodVars.add("com.weaver.esb.setting.code.controller.EsbCodeController");
        noGoodVars.add("com.weaver.esb.setting.component.controller.GetRequestController");
        noGoodVars.add("com.weaver.esb.setting.component.controller.OpenApiController");
        noGoodVars.add("com.weaver.esb.setting.component.service.CrmApiServiceImpl");
        noGoodVars.add("com.weaver.esb.setting.webService.controller.WsInterfaceController");
        noGoodVars.add("com.weaver.escheduler.admin.controller.JobGroupController");
        noGoodVars.add("com.weaver.escheduler.admin.controller.JobGroupNewController");
        noGoodVars.add("com.weaver.escheduler.admin.controller.JobLogController");
        noGoodVars.add("com.weaver.escheduler.admin.controller.JobLogNewController");
        noGoodVars.add("com.weaver.escheduler.admin.controller.UserJobCallbackController");
        noGoodVars.add("com.weaver.escheduler.admin.controller.UserJobLogController");
        noGoodVars.add("com.weaver.escheduler.admin.service.impl.ESchedulerServiceImpl");
        noGoodVars.add("com.weaver.esearch.data.common.init.elasticsearch.ILMInitExcutor");
        noGoodVars.add("com.weaver.esearch.data.common.init.elasticsearch.IndexInitExcutor");
        noGoodVars.add("com.weaver.esearch.data.common.init.elasticsearch.PipelineInitExcutor");
        noGoodVars.add("com.weaver.esearch.data.config.CommonValues");
        noGoodVars.add("com.weaver.esearch.search.devops.ESRestApiDevOpsController");
        noGoodVars.add("com.weaver.excel.formula.service.impl.CustomFuncServiceImpl");
        noGoodVars.add("com.weaver.excel.formula.service.impl.ExpressFormulaServiceImpl");
        noGoodVars.add("com.weaver.excel.formula.service.impl.TempServiceImpl");
        noGoodVars.add("com.weaver.fna.bank.component.SaveTransferInfoToReviewComponent");
        noGoodVars.add("com.weaver.fna.bank.manager.BankApiReportTemplate");
        noGoodVars.add("com.weaver.fna.bank.manager.BankApiTransferTemplate");
        noGoodVars.add("com.weaver.fna.bank.manager.CMBCBS.CMBCBSERCRCQRYReport");
        noGoodVars.add("com.weaver.fna.bank.manager.CMBCBS.CMBCBSERCURDTLReport");
        noGoodVars.add("com.weaver.fna.bank.manager.CMBCBS.CMBCBSERDRCQRYReport");
        noGoodVars.add("com.weaver.fna.bank.manager.CMBCBS.CMBCBSERPAYSAVTransfer");
        noGoodVars.add("com.weaver.fna.bank.manager.CMBCBS.CMBCBSERQRYTRSReport");
        noGoodVars.add("com.weaver.fna.bank.manager.CMBCBS.CMBCBSERWTQReport");
        noGoodVars.add("com.weaver.fna.bank.manager.HZBANKBT.HZBANKBTBY0001Transfer");
        noGoodVars.add("com.weaver.fna.bank.manager.TenOpenBank.TenOpenBankCreateBatchPrivateTransferTransfer");
        noGoodVars.add("com.weaver.fna.bank.manager.TenOpenBank.TenOpenBankCreateBatchTransferTransfer");
        noGoodVars.add("com.weaver.fna.bank.manager.TenOpenBank.TenOpenBankCreateTransferTransfer");
        noGoodVars.add("com.weaver.fna.expense.service.impl.DimensionPartServiceImpl");
        noGoodVars.add("com.weaver.fna.expense.service.impl.TemplateInformationServiceImpl");
        noGoodVars.add("com.weaver.form.action.RemotefreeformController");
        noGoodVars.add("com.weaver.form.action.UploadController");
        noGoodVars.add("com.weaver.framework.limit.WeaverLimitFilterConfig");
        noGoodVars.add("com.weaver.front.monitor.service.impl.es.EsMonitorSettingsServiceImpl");
        noGoodVars.add("com.weaver.intlogin.cache.IntLoginBaseCache");
        noGoodVars.add("com.weaver.intlogin.cache.IntLoginSettingCache");
        noGoodVars.add("com.weaver.intlogin.controller.intLoginEncryptController");
        noGoodVars.add("com.weaver.intlogin.controller.intLoginResetController");
        noGoodVars.add("com.weaver.intlogin.controller.intLoginSettingController");
        noGoodVars.add("com.weaver.intunifyauth.client.cas.config.ApplicationPropertiesConfig");
        noGoodVars.add("com.weaver.intunifyauth.client.oauth.config.ApplicationPropertiesConfig");
        noGoodVars.add("com.weaver.intunifyauth.client.saml.config.ApplicationPropertiesConfig");
        noGoodVars.add("com.weaver.intunifyauth.client.webseal.config.ApplicationPropertiesConfig");
        noGoodVars.add("com.weaver.mail.send.util.MailInfoSaveUtils");
        noGoodVars.add("com.weaver.mc.handle.service.im.applyImpl.JoinApply");
        noGoodVars.add("com.weaver.meeting.controller.MeetingAttentionController");
        noGoodVars.add("com.weaver.meeting.controller.MeetingBaseSetController");
        noGoodVars.add("com.weaver.meeting.controller.MeetingDefinedSetController");
        noGoodVars.add("com.weaver.meeting.controller.MeetingDetailController");
        noGoodVars.add("com.weaver.meeting.controller.MeetingMonitorController");
        noGoodVars.add("com.weaver.meeting.controller.MeetingOpenApiController");
        noGoodVars.add("com.weaver.meeting.controller.MeetingSecauthListController");
        noGoodVars.add("com.weaver.meeting.controller.MeetingSecauthSetController");
        noGoodVars.add("com.weaver.meeting.controller.app.MeetingBaseSetAppController");
        noGoodVars.add("com.weaver.meeting.controller.app.MeetingDefinedSetAppController");
        noGoodVars.add("com.weaver.meeting.controller.app.MeetingDetailAppController");
        noGoodVars.add("com.weaver.meeting.dao.DaoStoreProvider");
        noGoodVars.add("com.weaver.meeting.mergeCommon.InitMeetignApp");
        noGoodVars.add("com.weaver.meeting.thread.CancelRepeatThread");
        noGoodVars.add("com.weaver.meeting.thread.CreateShareByPermTransferThread");
        noGoodVars.add("com.weaver.meeting.thread.DealMeetingSecurityHistoryDataThread");
        noGoodVars.add("com.weaver.meeting.util.IMMessageUtil");
        noGoodVars.add("com.weaver.meeting.util.MeetingBaseServiceUtils");
        noGoodVars.add("com.weaver.meeting.util.MeetingDetachUtil");
        noGoodVars.add("com.weaver.meeting.util.MeetingPrmUtil");
        noGoodVars.add("com.weaver.meeting.util.MeetingRepeatServiceUtil");
        noGoodVars.add("com.weaver.odoc.service.impl.odocnumber.OdocNumberServiceImpl");
        noGoodVars.add("com.weaver.odoc.service.impl.odocnumber.OdocReservedNumberServiceImpl");
        noGoodVars.add("com.weaver.odoc.service.impl.wellsign.OdocWellSignSetServiceImpl");
        noGoodVars.add("com.weaver.odoc.util.basesetting.OfficialSettingUtil");
        noGoodVars.add("com.weaver.placard.controller.PlacardController");
        noGoodVars.add("com.weaver.portal.service.impl.HomepageServiceImpl");
        noGoodVars.add("com.weaver.project.domain.ProjectDubboReferences");
        noGoodVars.add("com.weaver.project.service.task.TaskService");
        noGoodVars.add("com.weaver.redis.RedisDsReentrantLock");
        noGoodVars.add("com.weaver.server.controller.AuthServerBaseController");
        noGoodVars.add("com.weaver.signcenter.service.message.impl.SignMessageServiceImpl");
        noGoodVars.add("com.weaver.statistics.utils.LogUtils");
        noGoodVars.add("com.weaver.teams.base.user.EmployeeServiceImpl");
        noGoodVars.add("com.weaver.teams.basic.service.init.BaseInitializeService");
        noGoodVars.add("com.weaver.teams.blog.action.app.mvc.controller.BlogController");
        noGoodVars.add("com.weaver.teams.blog.dao.BlogInfoServiceImpl");
        noGoodVars.add("com.weaver.teams.blog.dao.BlogServiceImpl");
        noGoodVars.add("com.weaver.teams.blog.trigger.NewBlogPushSubscriptJob");
        noGoodVars.add("com.weaver.teams.crm.agency.CrmAgencyController");
        noGoodVars.add("com.weaver.teams.crm.base.CrmController");
        noGoodVars.add("com.weaver.teams.crm.capital.CapitalAccountSettingController");
        noGoodVars.add("com.weaver.teams.crm.capital.CapitalController");
        noGoodVars.add("com.weaver.teams.crm.capital.CapitalMobileController");
        noGoodVars.add("com.weaver.teams.crm.capital.CapitalRemoteController");
        noGoodVars.add("com.weaver.teams.crm.clue.ClueController");
        noGoodVars.add("com.weaver.teams.crm.clue.ClueElementController");
        noGoodVars.add("com.weaver.teams.crm.clue.ClueMobileController");
        noGoodVars.add("com.weaver.teams.crm.clue.CluePoolController");
        noGoodVars.add("com.weaver.teams.crm.common.CommonController");
        noGoodVars.add("com.weaver.teams.crm.common.CommonMobileController");
        noGoodVars.add("com.weaver.teams.crm.common.CrmCommonController");
        noGoodVars.add("com.weaver.teams.crm.common.CrmCommonRemoteController");
        noGoodVars.add("com.weaver.teams.crm.common.CrmShareEntryController");
        noGoodVars.add("com.weaver.teams.crm.common.RemoteController");
        noGoodVars.add("com.weaver.teams.crm.common.apprvoefrom.CrmApproveFormController");
        noGoodVars.add("com.weaver.teams.crm.common.assignfield.AssignFieldController");
        noGoodVars.add("com.weaver.teams.crm.common.autonumber.CrmAutonumberController");
        noGoodVars.add("com.weaver.teams.crm.common.basesetting.CrmBaseSettingController");
        noGoodVars.add("com.weaver.teams.crm.common.configvalue.CrmConfigValueController");
        noGoodVars.add("com.weaver.teams.crm.common.createtype.CrmCreateTypeController");
        noGoodVars.add("com.weaver.teams.crm.common.dictionarysetting.DictionaryController");
        noGoodVars.add("com.weaver.teams.crm.common.entity.CrmCommonEntityMobileController");
        noGoodVars.add("com.weaver.teams.crm.common.entity.CrmEntityMobileController");
        noGoodVars.add("com.weaver.teams.crm.common.export.CrmExportController");
        noGoodVars.add("com.weaver.teams.crm.common.exportlog.ExportLogController");
        noGoodVars.add("com.weaver.teams.crm.common.field.CrmFieldController");
        noGoodVars.add("com.weaver.teams.crm.common.fieldmapping.CrmFieldMappingController");
        noGoodVars.add("com.weaver.teams.crm.common.freeform.CrmFreeFormController");
        noGoodVars.add("com.weaver.teams.crm.common.freeform.CrmFreeFormMobileController");
        noGoodVars.add("com.weaver.teams.crm.common.imagesetting.CrmImageSettingController");
        noGoodVars.add("com.weaver.teams.crm.common.intervention.CrmInterventionController");
        noGoodVars.add("com.weaver.teams.crm.common.leftMenu.CrmLeftMenuController");
        noGoodVars.add("com.weaver.teams.crm.common.mail.CrmMailController");
        noGoodVars.add("com.weaver.teams.crm.common.menu.CrmMenuController");
        noGoodVars.add("com.weaver.teams.crm.common.namesetting.NameSettingController");
        noGoodVars.add("com.weaver.teams.crm.common.newdistribution.NewDistributionController");
        noGoodVars.add("com.weaver.teams.crm.common.operationHistory.CrmCustomerSettingTrans");
        noGoodVars.add("com.weaver.teams.crm.common.print.CrmPrintController");
        noGoodVars.add("com.weaver.teams.crm.common.rel.CrmRelController");
        noGoodVars.add("com.weaver.teams.crm.common.remind.CrmRemindController");
        noGoodVars.add("com.weaver.teams.crm.common.shareapply.CrmShareapplyController");
        noGoodVars.add("com.weaver.teams.crm.common.sharerule.CrmShareRuleController");
        noGoodVars.add("com.weaver.teams.crm.common.summaryfield.CrmSummaryFieldController");
        noGoodVars.add("com.weaver.teams.crm.common.switchmoney.SwitchMoneyController");
        noGoodVars.add("com.weaver.teams.crm.common.tenantlog.CrmTenantLogController");
        noGoodVars.add("com.weaver.teams.crm.common.totalsizesetting.TotalSizeSettingController");
        noGoodVars.add("com.weaver.teams.crm.common.tree.CrmTreeController");
        noGoodVars.add("com.weaver.teams.crm.common.uisetting.CrmUiSettingController");
        noGoodVars.add("com.weaver.teams.crm.competitor.CompetitorController");
        noGoodVars.add("com.weaver.teams.crm.competitor.CompetitorMobileController");
        noGoodVars.add("com.weaver.teams.crm.competitor.CompetitorRemoteController");
        noGoodVars.add("com.weaver.teams.crm.competitor.RelationCompetitorController");
        noGoodVars.add("com.weaver.teams.crm.competitor.RelationCompetitorMobileController");
        noGoodVars.add("com.weaver.teams.crm.contact.ContactController");
        noGoodVars.add("com.weaver.teams.crm.contact.ContactElementController");
        noGoodVars.add("com.weaver.teams.crm.contact.ContactMobileController");
        noGoodVars.add("com.weaver.teams.crm.contact.ContactRemoteController");
        noGoodVars.add("com.weaver.teams.crm.contactremind.ContactRemindController");
        noGoodVars.add("com.weaver.teams.crm.contactremind.ContactRemindMobileController");
        noGoodVars.add("com.weaver.teams.crm.contactremind.RemindSettingController");
        noGoodVars.add("com.weaver.teams.crm.contract.ContractController");
        noGoodVars.add("com.weaver.teams.crm.contract.ContractInvoiceController");
        noGoodVars.add("com.weaver.teams.crm.contract.ContractMobileController");
        noGoodVars.add("com.weaver.teams.crm.contract.ContractPayController");
        noGoodVars.add("com.weaver.teams.crm.contract.ContractReceiveController");
        noGoodVars.add("com.weaver.teams.crm.contract.ContractRemoteController");
        noGoodVars.add("com.weaver.teams.crm.contract.ContractStageController");
        noGoodVars.add("com.weaver.teams.crm.contract.ContractTypeSettingController");
        noGoodVars.add("com.weaver.teams.crm.contract.flow.ContractFlowController");
        noGoodVars.add("com.weaver.teams.crm.contractapproved.ContractVersionController");
        noGoodVars.add("com.weaver.teams.crm.contractremind.ContractRemindController");
        noGoodVars.add("com.weaver.teams.crm.contractremind.ContractRemindMobileController");
        noGoodVars.add("com.weaver.teams.crm.contracttemplate.ContractFileTemplateController");
        noGoodVars.add("com.weaver.teams.crm.contracttemplate.relitemmark.ContractFileTemplateItemMarkController");
        noGoodVars.add("com.weaver.teams.crm.crmdicshare.CrmDicShareController");
        noGoodVars.add("com.weaver.teams.crm.crmsummary.CrmSummaryController");
        noGoodVars.add("com.weaver.teams.crm.crmsummary.comment.CommentSummaryController");
        noGoodVars.add("com.weaver.teams.crm.customcolumn.CustomColumnController");
        noGoodVars.add("com.weaver.teams.crm.customer.CustomerController");
        noGoodVars.add("com.weaver.teams.crm.customer.CustomerMobileController");
        noGoodVars.add("com.weaver.teams.crm.customer.CustomerRemoteController");
        noGoodVars.add("com.weaver.teams.crm.customer.addressManagement.CustomerAddressServiceImpl");
        noGoodVars.add("com.weaver.teams.crm.customer.search.CustomerSearchServiceImpl");
        noGoodVars.add("com.weaver.teams.crm.customer.value.CrmValuationController");
        noGoodVars.add("com.weaver.teams.crm.customersetting.CustomerSettingController");
        noGoodVars.add("com.weaver.teams.crm.customersetting.CustomerSettingShareController");
        noGoodVars.add("com.weaver.teams.crm.customform.CrmLayoutController");
        noGoodVars.add("com.weaver.teams.crm.custommode.CustommodeController");
        noGoodVars.add("com.weaver.teams.crm.defaultshare.ShareEntryServiceImpl");
        noGoodVars.add("com.weaver.teams.crm.demodata.DemoDataController");
        noGoodVars.add("com.weaver.teams.crm.distributionsetting.DistributionSettingController");
        noGoodVars.add("com.weaver.teams.crm.extend.common.sharerule.CrmShareRuleController");
        noGoodVars.add("com.weaver.teams.crm.extend.defaultshare.ShareEntryServiceImpl");
        noGoodVars.add("com.weaver.teams.crm.extend.exportdata.ExportController");
        noGoodVars.add("com.weaver.teams.crm.extend.exportdata.ExportFormServiceImpl");
        noGoodVars.add("com.weaver.teams.crm.extend.exportdata.customerexport.CustomerExportController");
        noGoodVars.add("com.weaver.teams.crm.externaldynamic.ExternalDynamicController");
        noGoodVars.add("com.weaver.teams.crm.externaldynamic.ExternalDynamicMobileController");
        noGoodVars.add("com.weaver.teams.crm.fieldsetting.FieldCopyController");
        noGoodVars.add("com.weaver.teams.crm.fieldsetting.FieldHiddenController");
        noGoodVars.add("com.weaver.teams.crm.fieldsetting.FieldSettingController");
        noGoodVars.add("com.weaver.teams.crm.h5index.H5CardSettingController");
        noGoodVars.add("com.weaver.teams.crm.h5index.H5DBMobileController");
        noGoodVars.add("com.weaver.teams.crm.h5index.H5IndexMobileController");
        noGoodVars.add("com.weaver.teams.crm.h5index.H5RemindSettingController");
        noGoodVars.add("com.weaver.teams.crm.h5index.H5menuDbMobileController");
        noGoodVars.add("com.weaver.teams.crm.h5index.H5menuMobileController");
        noGoodVars.add("com.weaver.teams.crm.importsetting.CrmImportPermissionController");
        noGoodVars.add("com.weaver.teams.crm.importsetting.CrmImportSettingController");
        noGoodVars.add("com.weaver.teams.crm.mail.EmailInfoController");
        noGoodVars.add("com.weaver.teams.crm.mail.EmployeeEmailController");
        noGoodVars.add("com.weaver.teams.crm.marketactivity.MarketActivityRemoteController");
        noGoodVars.add("com.weaver.teams.crm.marketactivity.MarketactivityController");
        noGoodVars.add("com.weaver.teams.crm.marketactivity.MarketactivityMobileController");
        noGoodVars.add("com.weaver.teams.crm.menusetting.MenuSettingController");
        noGoodVars.add("com.weaver.teams.crm.multi.MultiLangServiceImpl");
        noGoodVars.add("com.weaver.teams.crm.onoffsetting.CrmSettingOnOffController");
        noGoodVars.add("com.weaver.teams.crm.opendocking.OpenDockingCenterController");
        noGoodVars.add("com.weaver.teams.crm.opensea.OpenSeaController");
        noGoodVars.add("com.weaver.teams.crm.operatesetting.CrmOperateSettingController");
        noGoodVars.add("com.weaver.teams.crm.orderform.OrderformController");
        noGoodVars.add("com.weaver.teams.crm.orderform.OrderformMobileController");
        noGoodVars.add("com.weaver.teams.crm.orderform.OrderformRemoteController");
        noGoodVars.add("com.weaver.teams.crm.orderform.OrderformTypeSettingController");
        noGoodVars.add("com.weaver.teams.crm.price.PriceController");
        noGoodVars.add("com.weaver.teams.crm.price.PriceMobileController");
        noGoodVars.add("com.weaver.teams.crm.price.PriceRemoteController");
        noGoodVars.add("com.weaver.teams.crm.priceapproval.PriceApprovalController");
        noGoodVars.add("com.weaver.teams.crm.priceversion.PriceVersionController");
        noGoodVars.add("com.weaver.teams.crm.production.ProductionController");
        noGoodVars.add("com.weaver.teams.crm.production.ProductionMobileController");
        noGoodVars.add("com.weaver.teams.crm.production.ProductionRemoteController");
        noGoodVars.add("com.weaver.teams.crm.property.CustomerpropertyController");
        noGoodVars.add("com.weaver.teams.crm.property.CustomerpropertyMobileController");
        noGoodVars.add("com.weaver.teams.crm.quote.QuoteController");
        noGoodVars.add("com.weaver.teams.crm.quote.QuoteMobileController");
        noGoodVars.add("com.weaver.teams.crm.quote.QuoteRemoteController");
        noGoodVars.add("com.weaver.teams.crm.quoteapproval.QuoteApprovalController");
        noGoodVars.add("com.weaver.teams.crm.quoteversion.QuoteVersionController");
        noGoodVars.add("com.weaver.teams.crm.receive.ReceiveController");
        noGoodVars.add("com.weaver.teams.crm.recyclebin.RecycleBinController");
        noGoodVars.add("com.weaver.teams.crm.recyclebin.RecycleBinMobileController");
        noGoodVars.add("com.weaver.teams.crm.reportcustomer.CrmReportUserController");
        noGoodVars.add("com.weaver.teams.crm.requiredsetting.RequiredSettingController");
        noGoodVars.add("com.weaver.teams.crm.salechance.SaleChanceController");
        noGoodVars.add("com.weaver.teams.crm.salechance.SaleChanceMobileController");
        noGoodVars.add("com.weaver.teams.crm.salechance.SaleChanceRemoteController");
        noGoodVars.add("com.weaver.teams.crm.salechance.SaleProcessMobileController");
        noGoodVars.add("com.weaver.teams.crm.synergy.CrmSynSettingController");
        noGoodVars.add("com.weaver.teams.crm.target.SaleTargetController");
        noGoodVars.add("com.weaver.teams.crm.tools.CrmEsSearchService");
        noGoodVars.add("com.weaver.teams.datarule.core.config.api.ApiFactory");
        noGoodVars.add("com.weaver.teams.doc.e10.app.controller.DocumentChartDataAppController");
        noGoodVars.add("com.weaver.teams.doc.e10.app.controller.DocumentE10AppController");
        noGoodVars.add("com.weaver.teams.doc.e10.app.controller.DocumentSubscribeAppController");
        noGoodVars.add("com.weaver.teams.doc.e10.app.controller.DocumentsE10AppController");
        noGoodVars.add("com.weaver.teams.doc.e10.service.impl.DocumentDetailsServiceImpl");
        noGoodVars.add("com.weaver.teams.doc.e10.service.impl.DocumentServiceImpl");
        noGoodVars.add("com.weaver.teams.doc.e10.service.impl.DocumentTrashbinServiceImpl");
        noGoodVars.add("com.weaver.teams.doc.e10.service.impl.PermissionServiceImpl");
        noGoodVars.add("com.weaver.teams.doc.e10.web.controller.DocumentChartDataController");
        noGoodVars.add("com.weaver.teams.doc.e10.web.controller.DocumentE10Controller");
        noGoodVars.add("com.weaver.teams.doc.e10.web.controller.DocumentsE10Controller");
        noGoodVars.add("com.weaver.teams.doc.e10.web.controller.openapi.DocOpenAPIDocumentController");
        noGoodVars.add("com.weaver.teams.esb.EsbloginController");
        noGoodVars.add("com.weaver.teams.file.e10.manager.file.FileInfoManagerImpl");
        noGoodVars.add("com.weaver.teams.file.e10.manager.file.FileSaveManagerImpl");
        noGoodVars.add("com.weaver.teams.file.e10.manager.im.ImMessageManagerImpl");
        noGoodVars.add("com.weaver.teams.file.e10.manager.upload.ReUploadManagerImpl");
        noGoodVars.add("com.weaver.teams.file.e10.manager.upload.UploadManagerImpl");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.web.GetFileController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.web.VideoTranscodeController");
        noGoodVars.add("com.weaver.teams.file.e10.service.web.impl.E10PublicUploadServiceImpl");
        noGoodVars.add("com.weaver.teams.file.e10.service.web.impl.E10ReUploadServiceImpl");
        noGoodVars.add("com.weaver.teams.file.timing.task.FileObjSpaceTask");
        noGoodVars.add("com.weaver.teams.file.utils.FfmpegUtils");
        noGoodVars.add("com.weaver.teams.hrm.service.hrmconfigset.HrmConfigSetServiceImpl");
        noGoodVars.add("com.weaver.teams.hrm.service.syncdata.validate.HrmRelationValidatorFactory");
        noGoodVars.add("com.weaver.teams.hrm.wrapper.initNumber.HrmInitNumberWrapper");
        noGoodVars.add("com.weaver.teams.im.ImSendServiceImpl");
        noGoodVars.add("com.weaver.teams.paas.PaasremoteController");
        noGoodVars.add("com.weaver.teams.paas.PsloginController");
        noGoodVars.add("com.weaver.voice.controller.DictController");
        noGoodVars.add("com.weaver.voice.controller.IntentionController");
        noGoodVars.add("com.weaver.voice.controller.TenantDataInitController");
        noGoodVars.add("com.weaver.voice.controller.VoiceController");
        noGoodVars.add("com.weaver.voice.controller.VoiceSettingController");
        noGoodVars.add("com.weaver.workflow.common.util.TenantUtil");
        noGoodVars.add("com.weaver.workflow.core.util.msg.RequestSendMsgService");
        noGoodVars.add("com.weaver.workflow.pathdef.global.util.auth.JugdeAuthUtil");
        noGoodVars.add("com.weaver.workrelate.performance.service.impl.PerformanceReportServiceImpl");
        noGoodVars.add("com.weaver.teams.crm.common.esb.RemoteEsbToCrmServiceImpl");
        noGoodVars.add("com.weaver.teams.doc.e10.service.impl.DocumentSubscribeServiceImpl");
        noGoodVars.add("com.weaver.teams.doc.e10.service.dubbo.DocClientServiceImpl");
        noGoodVars.add("com.weaver.esb.setting.rest.impl.EsbActionFlowRestImpl");
        noGoodVars.add("com.weaver.esearch.data.core.schema.collector.common.CommonCollectorUtil");
        noGoodVars.add("com.weaver.excel.formula.service.impl.ExcelFormulasServiceImpl");
        noGoodVars.add("com.weaver.excel.formula.service.impl.ExcelformulaServiceImpl");
        noGoodVars.add("com.weaver.common.form.fieldmanage.service.impl.FormCustomColumnServiceImpl");
        noGoodVars.add("com.weaver.teams.file.service.RemoteUploadServiceImpl");
        noGoodVars.add("com.weaver.common.form.formmanage.service.impl.FormManageCustomColumnServiceImpl");
        noGoodVars.add("com.weaver.teams.formreport.aspect.ReportFormManageCustomColumnServiceImpl");
        noGoodVars.add("com.weaver.intlogin.common.otherface.UploadRemoteFileRightService");
        noGoodVars.add("com.weaver.meeting.service.impl.ExShareServiceImpl");
        noGoodVars.add("com.weaver.meeting.permissionTransfer.MeetingPermissionTransfer");
        noGoodVars.add("com.weaver.meeting.api.rest.impl.MeetingRemoteBaseServiceImpl");
        noGoodVars.add("com.weaver.framework.limit.WeaverLimitFilterConfig");
        noGoodVars.add("com.weaver.teams.attend.service.TimecardServiceImpl");
        noGoodVars.add("com.weaver.teams.file.e10.manager.preview.VideoPreview");
        noGoodVars.add("com.weaver.teams.blog.dao.BlogRemindDataServiceImpl");
        noGoodVars.add("com.weaver.basecommon.comment.service.impl.NewCommonCommentServiceImpl");
        noGoodVars.add("com.weaver.calendar.controller.CalendarCommonShareController");
        noGoodVars.add("com.weaver.common.cache.tablecache.config.ComInfoCacheExtendConfig");
        noGoodVars.add("com.weaver.crm.market.article.service.impl.ArticleServiceImpl");
        noGoodVars.add("com.weaver.datasecurity.service.impl.AuditLogArchivingFileServiceImpl");
        noGoodVars.add("com.weaver.ebuilder.form.view.list.service.impl.ListServiceImpl");
        noGoodVars.add("com.weaver.esb.component.crm.contract.service.impl.ContractInvoiceServiceImpl");
        noGoodVars.add("com.weaver.esb.component.crm.contract.service.impl.ContractPayServiceImpl");
        noGoodVars.add("com.weaver.esb.component.crm.contract.service.impl.ContractReceiveServiceImpl");
        noGoodVars.add("com.weaver.esb.component.crm.contract.service.impl.ContractStageServiceImpl");
        noGoodVars.add("com.weaver.esearch.data.config.module.register.RegisterModuleMQConfiguration");
        noGoodVars.add("com.weaver.excel.formula.controller.ExcelController");
        noGoodVars.add("com.weaver.fna.bank.util.OpenAccountDetailUtil");
        noGoodVars.add("com.weaver.fna.expense.util.FexsInvoiceInfoUtil");
        noGoodVars.add("com.weaver.fna.expense.util.FexsInvoicePrivateUtil");
        noGoodVars.add("com.weaver.framework.config.WeaverCacheMonitorConfig");
        noGoodVars.add("com.weaver.inc.parser.thirdparty.BaiLiServiceImpl");
        noGoodVars.add("com.weaver.intexchange.cache.IntExchangeBaseCache");
        noGoodVars.add("com.weaver.intexchange.cache.IntExchangeBaseSettingCache");
        noGoodVars.add("com.weaver.intexchange.cache.IntExchangeMeetingRoomCache");
        noGoodVars.add("com.weaver.intexchange.cache.IntExchangeUserSettingCache");
        noGoodVars.add("com.weaver.mail.receive.service.impl.EmailReceiveServiceImpl");
        noGoodVars.add("com.weaver.meeting.service.operate.CreateRoomUsageEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingEncSetFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingLogsEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingMonitorFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingNewRemindSetDataListEndEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingNewRemindSetDataListEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingNewRemindSetFieldEndEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingNewRemindSetFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingNewRemindSetFieldOnlyEndEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingNewRemindSetFieldOnlyEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingRemindPersonFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingRemindTemplateFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingRepeatDetailEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingRepeatFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingRoomFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingRoomScreenSetFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingRoomShareFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingSeatCardMouldFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingSeatCardPrintRealSetEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingSeatCardPrintSetEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingSeatCardSaveAsSetEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingSecAuthListSetFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingSecAuthSetFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingSecAuthShareSetFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingServiceProFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingServiceTypeFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingSetFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingShareFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingSignSetFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingTaskFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingVideoFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingVideoLineFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetMeetingVideoSetFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetRoomUsageColumnDataEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.GetRoomUsageDataEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.MeetingDataReadyEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.MeetingDetailEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.MeetingEsbBeforeEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.MeetingEsbEndEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.MeetingMemberEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.MeetingOperateDataReadyEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.MeetingPermissionEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.MeetingReceiptEsbEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.MeetingRemindEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.MeetingRepeatStoreEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.MeetingSeatConfFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.MeetingSecurityDataEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.MeetingServiceEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.MeetingServiceFieldListEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.MeetingServiceFormEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.MeetingShareEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.MeetingSignEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.MeetingTopicDateFormEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.MeetingTopicEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.MeetingTopicFieldListEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.MeetingTopicFormEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.ReceiptWorkflowEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.SendESearchMQEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.SubmitMeetingDetailEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.TurnedToOtherModule");
        noGoodVars.add("com.weaver.meeting.service.operate.UpdateMeetingStatusEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.WorkflowEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.meetingChk.MeetingChkMemberEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.meetingChk.MeetingChkRoomAttributeEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.meetingChk.MeetingChkRoomEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.meetingChk.MeetingChkServiceEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.meetingDefinedSet.CreateEBFormEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.meetingDefinedSet.CreateWFDefaultFormEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.meetingDefinedSet.GetDefinedDateFieldFormEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.meetingDefinedSet.GetDefinedDefaultFieldFormEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.meetingDefinedSet.GetDefinedEditListEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.meetingDefinedSet.GetDefinedFieldFormEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.meetingMember.CreateOtherMember2ReceiptEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.meetingMember.CreateReceiptWFEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.meetingMember.GetMeetingReceiptMemberFieldEvent");
        noGoodVars.add("com.weaver.meeting.service.operate.meetingMember.UpdateMemberReceiptEvent");
        noGoodVars.add("com.weaver.meeting.util.MeetingFieldUtil");
        noGoodVars.add("com.weaver.odoc.browser.standard.ReservedNumberBrowser");
        noGoodVars.add("com.weaver.server.config.CacheKeyConfig");
        noGoodVars.add("com.weaver.signature.FlowPostProcessor");
        noGoodVars.add("com.weaver.teams.attend.action.AttendAction");
        noGoodVars.add("com.weaver.teams.doc.e10.service.impl.DocListViewSettingServiceImpl");
        noGoodVars.add("com.weaver.teams.doc.e10.service.impl.DocumentConfigServiceImpl");
        noGoodVars.add("com.weaver.teams.doc.e10.service.impl.DocumentE10ServiceImpl");
        noGoodVars.add("com.weaver.teams.doc.e10.service.impl.FullSearchServiceImpl");
        noGoodVars.add("com.weaver.teams.file.BaseDownloadController");
        noGoodVars.add("com.weaver.teams.file.e10.manager.plug.base.FoxitPlug");
        noGoodVars.add("com.weaver.teams.file.e10.manager.plug.base.FuxinLightReadPlug");
        noGoodVars.add("com.weaver.teams.file.e10.manager.plug.base.Iweb2015Plug");
        noGoodVars.add("com.weaver.teams.file.e10.manager.plug.base.OfficeOnlinePlug");
        noGoodVars.add("com.weaver.teams.file.e10.manager.plug.base.OfficeWeb365Plug");
        noGoodVars.add("com.weaver.teams.file.e10.manager.plug.base.ShukeLightReadPlug");
        noGoodVars.add("com.weaver.teams.file.e10.manager.plug.base.SuwellPlug");
        noGoodVars.add("com.weaver.teams.file.e10.manager.plug.base.WpsClientPlug");
        noGoodVars.add("com.weaver.teams.file.e10.manager.plug.base.WpsPlug");
        noGoodVars.add("com.weaver.teams.file.e10.manager.plug.base.YozoPlug");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.app.DownloadAppController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.app.DownloadRemoteAppController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.app.E10AppDownloadController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.app.E10AppFileController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.app.E10AppUploadController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.app.UploadAppController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.app.UploadRemoteAppController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.web.DownloadController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.web.DownloadRemoteController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.web.E10DownloadController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.web.E10FileController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.web.E10PublicDownloadController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.web.E10PublicFileController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.web.E10UploadController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.web.FileAppealController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.web.FileMonopolyController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.web.ImUploadController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.web.LoggerDownloadController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.web.LoggerUploadController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.web.PackDownloadController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.web.SignController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.web.TiffDLController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.web.UploadRemoteController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.web.VideoDLController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.web.openapi.E10OpenApiFileController");
        noGoodVars.add("com.weaver.teams.file.e10.mvc.web.openapi.E10OpenApiUploadController");
        noGoodVars.add("com.weaver.teams.file.e10.service.web.impl.DocumentLogServiceImpl");
        noGoodVars.add("com.weaver.teams.file.e10.service.web.impl.WpsUrlServiceImpl");
        noGoodVars.add("com.weaver.workflow.core.FlowPostProcessor");
        noGoodVars.add("com.weaver.workflow.pathdef.FlowPostProcessor");
        noGoodVars.add("com.weaver.workflow.pathdef.task.handler.WfpAddMinusDataUpgradeHandler");
        noGoodVars.add("com.weaver.workrelate.goal.service.impl.WrgmCommonServiceImpl");
        noGoodVars.add("com.weaver.workrelate.goal.service.impl.WrgmRemindServiceImpl");
        noGoodVars.add("com.weaver.esearch.search.service.homepage.impl.IHomePageServiceImpl");
        noGoodVars.add("com.weaver.esearch.data.core.schema.collector.mainline.MainLineUtil");
        noGoodVars.add("com.weaver.teams.file.service.UploadServiceImpl");
        noGoodVars.add("com.weaver.meeting.api.rest.impl.MeetingFormManagerImpl");
        noGoodVars.add("com.weaver.common.cache.laycache.config.WeaverCacheConfig");
    }

    public static Set<String> noGoodInit = new HashSet<>();

    static{
        noGoodInit.add("FormSettingLog");
        noGoodInit.add("HrmContextUtil");
        noGoodInit.add("accountLoggerTemplate");
        noGoodInit.add("addUpDeductionLoggerTemplate");
        noGoodInit.add("addUpSituationLoggerTemplate");
        noGoodInit.add("appComponentLoggerTemplate");
        noGoodInit.add("appExecuteLoggerTemplate");
        noGoodInit.add("appSceneLoggerTemplate");
        noGoodInit.add("appTypeLoggerTemplate");
        noGoodInit.add("appVariableLoggerTemplate");
        noGoodInit.add("applicationLoggerTemplate");
        noGoodInit.add("applicationServerLoggerTemplate");
        noGoodInit.add("attendQuoteFieldLoggerTemplate");
        noGoodInit.add("attendQuoteFieldSettingLoggerTemplate");
        noGoodInit.add("attendQuoteLoggerTemplate");
        noGoodInit.add("authServerCasLoggerTemplate");
        noGoodInit.add("authServerLoggerTemplate");
        noGoodInit.add("authServerOauthLoggerTemplate");
        noGoodInit.add("baseSettingLoggerTemplate");
        noGoodInit.add("base_common_core_springContext");
        noGoodInit.add("basic_springContext");
        noGoodInit.add("batchLogger");
        noGoodInit.add("bcwConfigLoggerTemplate");
        noGoodInit.add("casLoggerTemplate");
        noGoodInit.add("channelImportLogLogger");
        noGoodInit.add("channelLogger");
        noGoodInit.add("cleanLogHrLoggerTemplate");
        noGoodInit.add("clientContext");
        noGoodInit.add("cn.eteams.wechat.util.AdapterUtils");
        noGoodInit.add("cn.hutool.extra.spring.SpringUtil");
        noGoodInit.add("com.weaver.attendweb.util.AttendProgressUtil");
        noGoodInit.add("com.weaver.basic.common.util.MobileLocationUtil");
        noGoodInit.add("com.weaver.client.util.SpringContextBeansUtil");
        noGoodInit.add("com.weaver.common.async.util.ApplicationContextUtil");
        noGoodInit.add("com.weaver.common.baseserver.context.BaseServerSpringContextHolder");
        noGoodInit.add("com.weaver.common.cache.laycache.util.CacheSpringContextHolder");
        noGoodInit.add("com.weaver.common.component.browser.register.WeaBrowserRegister");
        noGoodInit.add("com.weaver.common.component.carddetailform.register.WeaCardDetailFormRegister");
        noGoodInit.add("com.weaver.common.distribution.lock.util.SpringUtilLock");
        noGoodInit.add("com.weaver.common.elog.service.ApplicationContextProvider");
        noGoodInit.add("com.weaver.common.elog.util.ElogHttpClients");
        noGoodInit.add("com.weaver.common.form.physical.util.SpringBeanUtils");
        noGoodInit.add("com.weaver.common.form.stat.register.dataStat.DataStatRegister");
        noGoodInit.add("com.weaver.common.ln.util.LN");
        noGoodInit.add("com.weaver.common.mybatis.monitor.interceptor.MonitorSqlContextHolder");
        noGoodInit.add("com.weaver.common.security.encryption.dao.GetAlgKey");
        noGoodInit.add("com.weaver.common.security.util.CryptoSpringContextHolder");
        noGoodInit.add("com.weaver.contentmoderation.config.ModerationNameConfig");
        noGoodInit.add("com.weaver.cowork.util.CoworkEmployeeUtils");
        noGoodInit.add("com.weaver.cowork.util.CoworkExtModuleUtils");
        noGoodInit.add("com.weaver.cowork.util.CoworkModuleUtils");
        noGoodInit.add("com.weaver.cowork.util.CoworkSpringContextUtil");
        noGoodInit.add("com.weaver.datasecurity.config.DataSecurityApplicationContext");
        noGoodInit.add("com.weaver.eb.client.oauth.OauthUtils");
        noGoodInit.add("com.weaver.eb.gateway.util.GatewayUtils");
        noGoodInit.add("com.weaver.ebuilder.common.cache.EbuilderCacheUtil");
        noGoodInit.add("com.weaver.ebuilder.common.installer.utils.SpringUtil");
        noGoodInit.add("com.weaver.ebuilder.common.packing.utils.SpringUtil");
        noGoodInit.add("com.weaver.ebuilder.common.spring.EbuilderSpringUtils");
        noGoodInit.add("com.weaver.ebuilder.common.util.FileDownloadUtil");
        noGoodInit.add("com.weaver.ebuilder.common.util.NoTenantUtil");
        noGoodInit.add("com.weaver.ebuilder.datasource.api.util.DataSourceSpringContext");
        noGoodInit.add("com.weaver.ebuilder.form.base.parser.condition.handler.CommonConditionHandler");
        noGoodInit.add("com.weaver.ebuilder.form.base.parser.datetrans.DateTransConverter");
        noGoodInit.add("com.weaver.ebuilder.form.base.utils.physicaltable.condition.handler.CommonConditionHandler");
        noGoodInit.add("com.weaver.ebuilder.form.base.utils.physicaltable.condition.handler.EbuilderEQConditionHandler");
        noGoodInit.add("com.weaver.ebuilder.form.common.utils.service.ApplicationContextHelper");
        noGoodInit.add("com.weaver.ebuilder.form.remote.util.ApiReqParamUtil");
        noGoodInit.add("com.weaver.ecode.util.ApplicationContextUtil");
        noGoodInit.add("com.weaver.edc.common.report.analysis.provider.ReportApplicationContextProvider");
        noGoodInit.add("com.weaver.em.base.common.util.EmI18nUtils");
        noGoodInit.add("com.weaver.em.msg.adapter.util.ImApiUtils");
        noGoodInit.add("com.weaver.em.msg.componet.register.MsgTempRegister");
        noGoodInit.add("com.weaver.esb.base.utils.EsbApplicationContextProvider");
        noGoodInit.add("com.weaver.esb.component.mq.builder.MqLogBuilder");
        noGoodInit.add("com.weaver.esb.server.core.impl.DefaultEsbEngine");
        noGoodInit.add("com.weaver.esb.setting.common.config.EsbNacosConfig");
        noGoodInit.add("com.weaver.esb.setting.common.util.EsbServiceInjectUtil");
        noGoodInit.add("com.weaver.esearch.search.util.ModuleUtil");
        noGoodInit.add("com.weaver.esearch.search.util.SearchIndexUtil");
        noGoodInit.add("com.weaver.eteams.file.client.config.FileSupportType");
        noGoodInit.add("com.weaver.excel.formula.init.ExcelSpringContext");
        noGoodInit.add("com.weaver.file.online.common.util.ApplicationContextBeanUtils");
        noGoodInit.add("com.weaver.fna.bank.common.util.SpringContextUtil");
        noGoodInit.add("com.weaver.fna.bank.manager.TenOpenBank.BankTenOpenBank");
        noGoodInit.add("com.weaver.framework.configcenter.sdk.inner.util.RedisUtil");
        noGoodInit.add("com.weaver.framework.rpc.security.SecurityContext");
        noGoodInit.add("com.weaver.inc.biz.api.util.MessageUtils");
        noGoodInit.add("com.weaver.inc.biz.service.impl.InvoiceOutShareServiceImpl");
        noGoodInit.add("com.weaver.inc.biz.util.BizCheckUtils");
        noGoodInit.add("com.weaver.inc.biz.util.BizUtils");
        noGoodInit.add("com.weaver.inc.biz.util.BsonUtils");
        noGoodInit.add("com.weaver.inc.biz.util.DetachUtils");
        noGoodInit.add("com.weaver.inc.biz.util.ExtUtils");
        noGoodInit.add("com.weaver.inc.biz.util.IncBrowserUtil");
        noGoodInit.add("com.weaver.inc.biz.util.IncYyBizUtils");
        noGoodInit.add("com.weaver.inc.biz.util.LoginUserUtils");
        noGoodInit.add("com.weaver.inc.biz.util.OcrValidUtils");
        noGoodInit.add("com.weaver.inc.biz.util.PermissionUtils");
        noGoodInit.add("com.weaver.inc.biz.util.SmartReviewUtils");
        noGoodInit.add("com.weaver.inc.biz.util.UseSettingCheckUtils");
        noGoodInit.add("com.weaver.inc.book.util.CbFylxUtils");
        noGoodInit.add("com.weaver.inc.book.util.IncBrowserUtil");
        noGoodInit.add("com.weaver.inc.common.component.cache.IncCacheTool");
        noGoodInit.add("com.weaver.inc.common.component.context.IncDepartmentContext");
        noGoodInit.add("com.weaver.inc.common.component.util.IncSystemUtils");
        noGoodInit.add("com.weaver.inc.common.component.util.IncYyUtils");
        noGoodInit.add("com.weaver.inc.common.component.util.MsgRestUtils");
        noGoodInit.add("com.weaver.inc.common.component.util.UploadFileUtils");
        noGoodInit.add("com.weaver.inc.common.http.HttpUtils");
        noGoodInit.add("com.weaver.inc.common.util.IncAdapterUtils");
        noGoodInit.add("com.weaver.inc.common.util.IncAsyncUtils");
        noGoodInit.add("com.weaver.inc.common.util.IncBizUtils");
        noGoodInit.add("com.weaver.inc.common.util.IncParserUtils");
        noGoodInit.add("com.weaver.inc.data.util.CacheUtils");
        noGoodInit.add("com.weaver.inc.data.util.CorpUtils");
        noGoodInit.add("com.weaver.inc.data.util.HrmCacheUtils");
        noGoodInit.add("com.weaver.inc.mail.util.SmsOrcUtils");
        noGoodInit.add("com.weaver.inc.parser.util.ParseRestUtils");
        noGoodInit.add("com.weaver.inc.proxy.util.ProxyUtils");
        noGoodInit.add("com.weaver.inc.proxy.util.SystemUtils");
        noGoodInit.add("com.weaver.intcenter.hr.util.SpringContextUtil");
        noGoodInit.add("com.weaver.intcenter.ldap.util.SpringContextUtil");
        noGoodInit.add("com.weaver.intcenter.mail.config.IntMailNacosConfig");
        noGoodInit.add("com.weaver.intlogin.common.until.SpringUtil");
        noGoodInit.add("com.weaver.intunifyauth.client.cas.util.SpringContextBeansUtil");
        noGoodInit.add("com.weaver.intunifyauth.client.oauth.util.SpringContextBeansUtil");
        noGoodInit.add("com.weaver.intunifyauth.client.saml.util.SpringContextBeansUtil");
        noGoodInit.add("com.weaver.intunifyauth.client.webseal.util.SpringContextBeansUtil");
        noGoodInit.add("com.weaver.loom.context.cache.BaseHolder");
        noGoodInit.add("com.weaver.loom.monitor.util.ApplicationContextUtil");
        noGoodInit.add("com.weaver.mail.base.common.FormOptions");
        noGoodInit.add("com.weaver.mail.base.datatransform.mailinfo.MailBaseSpringBeanUtil");
        noGoodInit.add("com.weaver.mail.core.util.MailCoreSpringBeanUtil");
        noGoodInit.add("com.weaver.mail.core.util.MailEmployeeUtils");
        noGoodInit.add("com.weaver.mail.core.util.MailModuleUtils");
        noGoodInit.add("com.weaver.mail.core.util.MailPermissionUtils");
        noGoodInit.add("com.weaver.mail.receive.util.MailReceiveSpringBeanUtil");
        noGoodInit.add("com.weaver.meeting.core.chain.util.MtSpringContextUtil");
        noGoodInit.add("com.weaver.odoc.util.odocNodeSet.odocNodeInfoBean.SpecificUtil");
        noGoodInit.add("com.weaver.passport.util.MobileLocationUtil");
        noGoodInit.add("com.weaver.print.core.util.PrintSpringUtil");
        noGoodInit.add("com.weaver.scene.cache.SceneCacheUtil");
        noGoodInit.add("com.weaver.server.util.SpringContextBeansUtil");
        noGoodInit.add("com.weaver.shardingspare.util.SpringUtil");
        noGoodInit.add("com.weaver.sms.component.SmsApplicationContextProvider");
        noGoodInit.add("com.weaver.teams.attend.utils.BatchDocumentMessageUtils");
        noGoodInit.add("com.weaver.teams.blog.util.SpringContextUtils");
        noGoodInit.add("com.weaver.teams.datarule.utils.SpringContextUtil");
        noGoodInit.add("com.weaver.teams.doc.e10.config.SpringContextUtils");
        noGoodInit.add("com.weaver.teams.doc.e10.service.impl.DocCacheServiceImpl");
        noGoodInit.add("com.weaver.teams.doc.e10.util.MessageQueueUtils");
        noGoodInit.add("com.weaver.teams.file.config.WpsConfig");
        noGoodInit.add("com.weaver.teams.jms.email.EmailSessionFactory");
        noGoodInit.add("com.weaver.teams.salary.service.email.SalaryEmailSessionFactory");
        noGoodInit.add("com.weaver.teams.security.context.TenantContext");
        noGoodInit.add("com.weaver.teams.storage.tencentyun.COSService");
        noGoodInit.add("com.weaver.tenant.base.SpringContext");
        noGoodInit.add("com.weaver.tenant.utils.MobileLocationUtil");
        noGoodInit.add("com.weaver.utils.SpringContextHolder");
        noGoodInit.add("com.weaver.workflow.formmanager.FormManagerConfig");
        noGoodInit.add("commonCommentLoggerTemplate");
        noGoodInit.add("companyVirtualLogger");
        noGoodInit.add("convertorloggerTemplate");
        noGoodInit.add("coworkCategoryLoggerTemplate");
        noGoodInit.add("coworkCommentMonitorLoggerTemplate");
        noGoodInit.add("coworkDiscussApproveLoggerTemplate");
        noGoodInit.add("coworkDiscussMonitorLoggerTemplate");
        noGoodInit.add("coworkItemApproveElogTemplate");
        noGoodInit.add("coworkMonitorLoggerTemplate");
        noGoodInit.add("coworkRecycleCommentLoggerTemplate");
        noGoodInit.add("coworkRecycleDiscussLoggerTemplate");
        noGoodInit.add("coworkSectionCreatorLoggerTemplate");
        noGoodInit.add("coworkSectionLoggerTemplate");
        noGoodInit.add("coworkSectionManagerLoggerTemplate");
        noGoodInit.add("coworkSectionParticipantsLoggerTemplate");
        noGoodInit.add("coworkSystemSettingLoggerTemplate");
        noGoodInit.add("customDataLoggerTemplate");
        noGoodInit.add("customDataSetLoggerTemplate");
        noGoodInit.add("customerservice_asynRule");
        noGoodInit.add("customerservice_common");
        noGoodInit.add("cwApplyInfoLoggerTemplate");
        noGoodInit.add("cwDiscussLoggerTemplate");
        noGoodInit.add("cwItemsInfoLoggerTemplate");
        noGoodInit.add("dataInterfaceHrLoggerTemplate");
        noGoodInit.add("departmentCodeLogger");
        noGoodInit.add("docJobExecutor");
        noGoodInit.add("eSchedulerExecutor");
        noGoodInit.add("ebCacheTool");
        noGoodInit.add("edcReportDevBaseLoggerTemplate");
        noGoodInit.add("elogAnnualLeaveLoggerTemplate");
        noGoodInit.add("elogAppealLoggerTemplate");
        noGoodInit.add("elogAttendBaseSettingloggerTemplate");
        noGoodInit.add("elogBaseConfigSyncSet");
        noGoodInit.add("elogConfigLoggerTemplate");
        noGoodInit.add("elogFlowConfigLoggerTemplate");
        noGoodInit.add("elogFormulaLoggerTemplate");
        noGoodInit.add("elogIntgImportLoggerTemplate");
        noGoodInit.add("elogIntgSyncLoggerTemplate");
        noGoodInit.add("elogLeaveRuleLoggerTemplate");
        noGoodInit.add("elogLieuLeaveDetailLoggerTemplate");
        noGoodInit.add("elogLieuLeaveLoggerTemplate");
        noGoodInit.add("elogMachineLoggerTemplate");
        noGoodInit.add("elogOrbitLoggerTemplate");
        noGoodInit.add("elogOrbitSyncSet");
        noGoodInit.add("elogOvertimeRuleLoggerTemplate");
        noGoodInit.add("elogReportColLoggerTemplate");
        noGoodInit.add("elogReportShareLoggerTemplate");
        noGoodInit.add("elogShiftLoggerTemplate");
        noGoodInit.add("elogVacationCustomRuleLoggerTemplate");
        noGoodInit.add("elogVacationDetailoggerTemplate");
        noGoodInit.add("elogVacationoggerTemplate");
        noGoodInit.add("emCacheUtils");
        noGoodInit.add("empImportLogLogger");
        noGoodInit.add("employeeAndOrgLogger");
        noGoodInit.add("employeeDeclareLoggerTemplate");
        noGoodInit.add("esSpringApplicationContext");
        noGoodInit.add("esdSpringApplicationContext");
        noGoodInit.add("esearchCommonUtilsSpringApplicationContext");
        noGoodInit.add("extEmployeeLoggerTemplate");
        noGoodInit.add("file_fileLoggerTemplate");
        noGoodInit.add("filterELoggerTemplate");
        noGoodInit.add("fnaAllocationLoggerTemplate");
        noGoodInit.add("fnaBankCMBCBSLogger");
        noGoodInit.add("fnaBankIdentityLogger");
        noGoodInit.add("fnaBankProviderSetLogger");
        noGoodInit.add("fnaBankRightSettingLogger");
        noGoodInit.add("fnaCarryOverObjLoggerTemplate");
        noGoodInit.add("fnaDecimalpalcesLoggerTemplate");
        noGoodInit.add("fnaFormSettingLoggerTemplate");
        noGoodInit.add("fnaLeftMenuSettingLoggerTemplate");
        noGoodInit.add("fnaLoanManageLoggerTemplate");
        noGoodInit.add("fnaOptionsettingLoggerTemplate");
        noGoodInit.add("fnaReceiveInfoLoggerTemplate");
        noGoodInit.add("fnaRequestOperateLoggerTemplate");
        noGoodInit.add("fnaRequestOperateShowLoggerTemplate");
        noGoodInit.add("fnaStandardLoggerTemplate");
        noGoodInit.add("fnaSubjectLoggerTemplate");
        noGoodInit.add("fnaSubsidyRuleInfoLoggerTemplate");
        noGoodInit.add("fnaSubsidyRulesLoggerTemplate");
        noGoodInit.add("fnaTemplateCarPublicSetLoggerTemplate");
        noGoodInit.add("fnaTemplateCloseLoggerTemplate");
        noGoodInit.add("fnaTemplateInforLoggerTemplate");
        noGoodInit.add("form_springContext");
        noGoodInit.add("getSpringcontext");
        noGoodInit.add("hrCommonLogger");
        noGoodInit.add("hrContactLogger");
        noGoodInit.add("hrTipLogger");
        noGoodInit.add("hrmBrowserCustomFieldLogger");
        noGoodInit.add("hrmCommonLogger");
        noGoodInit.add("hrmContactLogger");
        noGoodInit.add("hrmPrivacyLogger");
        noGoodInit.add("hrmPrivacySettingLogger");
        noGoodInit.add("hrmconfigsetLogger");
        noGoodInit.add("i18nContextUtil");
        noGoodInit.add("iasChannelLoggerTemplate");
        noGoodInit.add("iasVolumnLoggerTemplate");
        noGoodInit.add("ic_mail_springContextUtil");
        noGoodInit.add("icpContextUtil");
        noGoodInit.add("incBizUserUtils");
        noGoodInit.add("intExchangeMeetingRoomTemplate");
        noGoodInit.add("intExchangeSettingLoggerTemplate");
        noGoodInit.add("intLoginAccountLoggerTemplate");
        noGoodInit.add("intLoginEncryptLoggerTemplate");
        noGoodInit.add("intLoginLoggerTemplate");
        noGoodInit.add("intLoginResetLoggerTemplate");
        noGoodInit.add("interfaceLoggerTemplate");
        noGoodInit.add("interfacesLoggerTemplate");
        noGoodInit.add("intunifyAuthLoggerTemplate");
        noGoodInit.add("intunifyAuthOauthLoggerTemplate");
        noGoodInit.add("intunifyAuthSamlLoggerTemplate");
        noGoodInit.add("intunifyAuthWebSealLoggerTemplate");
        noGoodInit.add("intunifytodoServerSysInfoLoggerTemplate");
        noGoodInit.add("intunifytodoServerSysSettingLoggerTemplate");
        noGoodInit.add("intunifytodoServerSysWorkflowLoggerTemplate");
        noGoodInit.add("iutCCRequestLoggerTemplate");
        noGoodInit.add("iutClientConfigInfoLoggerTemplate");
        noGoodInit.add("jmsContext");
        noGoodInit.add("jobsetLogger");
        noGoodInit.add("layoutTempLoggerTemplate");
        noGoodInit.add("mailApproveAccountElogTemplate");
        noGoodInit.add("mailApproveBaseElogTemplate");
        noGoodInit.add("mailApproveRecordElogTemplate");
        noGoodInit.add("mailApproveSyncElogTemplate");
        noGoodInit.add("mailEnterpriseElogTemplate");
        noGoodInit.add("mailPortElogTemplate");
        noGoodInit.add("mailRemindAccountSetElogTemplate");
        noGoodInit.add("mailRemindBaseSetElogTemplate");
        noGoodInit.add("mailRemindRecordElogTemplate");
        noGoodInit.add("mailRemindTempSetElogTemplate");
        noGoodInit.add("mailSpaceElogTemplate");
        noGoodInit.add("mailSysTempElogTemplate");
        noGoodInit.add("mailSystemSetElogTemplate");
        noGoodInit.add("marketViewLoggerTemplate");
        noGoodInit.add("matrixDataLogger");
        noGoodInit.add("matrixLogger");
        noGoodInit.add("matrixMtrLogger");
        noGoodInit.add("mc_log");
        noGoodInit.add("mySpringContextHolder");
        noGoodInit.add("oauthLoggerTemplate");
        noGoodInit.add("offspaceLogger");
        noGoodInit.add("orm_core_springContext");
        noGoodInit.add("otherDeductionLoggerTemplate");
        noGoodInit.add("paymentAgencyLoggerTemplate");
        noGoodInit.add("pdfCnvLoggerTemplate");
        noGoodInit.add("remoteServiceSpringContextHolder");
        noGoodInit.add("resourceLoggerTemplate");
        noGoodInit.add("salaryAcctRecordLoggerTemplate");
        noGoodInit.add("salaryArchiveBatchAdjustLoggerTemplate");
        noGoodInit.add("salaryArchiveFieldLoggerTemplate");
        noGoodInit.add("salaryArchiveItemAdjustLoggerTemplate");
        noGoodInit.add("salaryArchiveLoggerTemplate");
        noGoodInit.add("salaryItemLoggerTemplate");
        noGoodInit.add("salarySendLoggerTemplate");
        noGoodInit.add("salarySobLoggerTemplate");
        noGoodInit.add("salaryTemplateLoggerTemplate");
        noGoodInit.add("sectionEncryptLoggerTemplate");
        noGoodInit.add("securityViewLoggerTemplate");
        noGoodInit.add("siAccountLoggerTemplate");
        noGoodInit.add("siArchivesLoggerTemplate");
        noGoodInit.add("siCategoryLoggerTemplate");
        noGoodInit.add("siSchemeLoggerTemplate");
        noGoodInit.add("smsBaseConfigLoggerTemplate");
        noGoodInit.add("smsExcelUtils");
        noGoodInit.add("smsLimitConfigLoggerTemplate");
        noGoodInit.add("smsSignConfigLoggerTemplate");
        noGoodInit.add("springContextUtils");
        noGoodInit.add("syncLogLoggerTemplate");
        noGoodInit.add("syncSettingHrLoggerTemplate");
        noGoodInit.add("sysMappingLoggerTemplate");
        noGoodInit.add("systemLoggerTemplate");
        noGoodInit.add("taxAgentLoggerTemplate");
        noGoodInit.add("taxDeclarationLoggerTemplate");
        noGoodInit.add("taxRateLoggerTemplate");
        noGoodInit.add("tomcatStatSpringContextHolder");
        noGoodInit.add("transformRuleHrLoggerTemplate");
        noGoodInit.add("vvvvLoggerTemplate");
        noGoodInit.add("weaver-em-base-elog-config");
        noGoodInit.add("weaver-em-msg-elog-config");
        noGoodInit.add("weaver_core_springContext");
        noGoodInit.add("wfcAgentSetLoggerTemplate");
        noGoodInit.add("wfcAutoApproveLoggerTemplate");
        noGoodInit.add("wfcLoggerTemplate");
        noGoodInit.add("wfcMinusOperatorLoggerTemplate");
        noGoodInit.add("wfcRecycleLoggerTemplate");
        noGoodInit.add("wfcRequestFlowLoggerTemplate");
        noGoodInit.add("wfpLoggerTemplate");
        noGoodInit.add("wfpNodeLoggerTemplate");
    }

}
