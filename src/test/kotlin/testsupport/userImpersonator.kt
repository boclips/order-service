package testsupport

import com.boclips.terry.config.security.UserRoles
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder

fun MockHttpServletRequestBuilder.asBackofficeStaff() = this.with(
    SecurityMockMvcRequestPostProcessors
        .user("backofficestaff")
        .roles(
            UserRoles.VIEW_ORDERS
        )
)