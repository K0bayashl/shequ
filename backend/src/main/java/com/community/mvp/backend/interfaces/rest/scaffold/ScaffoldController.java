package com.community.mvp.backend.interfaces.rest.scaffold;

import com.community.mvp.backend.application.scaffold.service.ScaffoldApplicationService;
import com.community.mvp.backend.common.api.ApiResponse;
import com.community.mvp.backend.domain.scaffold.model.ScaffoldEcho;
import com.community.mvp.backend.domain.scaffold.model.ScaffoldStatus;
import com.community.mvp.backend.infrastructure.security.CurrentUserContext;
import com.community.mvp.backend.interfaces.rest.scaffold.dto.EchoRequest;
import com.community.mvp.backend.interfaces.rest.scaffold.dto.EchoResponse;
import com.community.mvp.backend.interfaces.rest.scaffold.dto.SecurePingResponse;
import com.community.mvp.backend.interfaces.rest.scaffold.dto.StatusResponse;
import jakarta.validation.Valid;
import java.util.Arrays;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 脚手架探活与调试接口。
 * <p>用于环境探测、回显测试和鉴权链路连通性验证。</p>
 */
@RestController
@RequestMapping("/api/v1/scaffold")
public class ScaffoldController {

    private final ScaffoldApplicationService scaffoldApplicationService;
    private final Environment environment;
    private final CurrentUserContext currentUserContext;

    /**
     * 构造脚手架控制器。
     *
     * @param scaffoldApplicationService 脚手架应用服务
     * @param environment Spring 环境对象
     * @param currentUserContext 当前用户上下文
     */
    public ScaffoldController(
        ScaffoldApplicationService scaffoldApplicationService,
        Environment environment,
        CurrentUserContext currentUserContext
    ) {
        this.scaffoldApplicationService = scaffoldApplicationService;
        this.environment = environment;
        this.currentUserContext = currentUserContext;
    }

    /**
     * 获取运行时状态信息。
     *
     * @return 应用名、激活 profile 与开关状态
     */
    @GetMapping("/ping")
    public ApiResponse<StatusResponse> ping() {
        ScaffoldStatus status = scaffoldApplicationService.buildStatus(Arrays.asList(environment.getActiveProfiles()));
        StatusResponse response = new StatusResponse(
            status.application(),
            status.activeProfiles(),
            status.databaseEnabled(),
            status.jwtEnabled(),
            status.redisEnabled(),
            status.openapiEnabled(),
            status.generatedAt()
        );
        return ApiResponse.success(response);
    }

    /**
     * 回显输入消息，用于连通性验证。
     *
     * @param request 回显请求
     * @return 回显结果
     */
    @PostMapping("/echo")
    public ApiResponse<EchoResponse> echo(@Valid @RequestBody EchoRequest request) {
        ScaffoldEcho echo = scaffoldApplicationService.echo(request.message());
        return ApiResponse.success(new EchoResponse(echo.message(), echo.echoedAt()));
    }

    /**
     * 鉴权探活接口。
     *
     * @return 当前主体信息
     */
    @GetMapping("/secure-ping")
    public ApiResponse<SecurePingResponse> securePing() {
        String principal = currentUserContext.currentUserId().orElse("anonymous");
        return ApiResponse.success(new SecurePingResponse(true, principal));
    }
}

