package ro.editii.scriptorium.web

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import ro.editii.scriptorium.RunStuffOnStartup
import ro.editii.scriptorium.TestConfig
import ro.editii.scriptorium.dao.AuthorRepository
import ro.editii.scriptorium.dao.RelocationRepository
import ro.editii.scriptorium.dao.TeiDivRepository
import ro.editii.scriptorium.dao.TeiFileRepository
import ro.editii.scriptorium.rest.GroovyShellConfig
import ro.editii.scriptorium.rest.GroovyShellRestController
import ro.editii.scriptorium.service.AdminService
import ro.editii.scriptorium.tei.TeiRepo

import javax.persistence.EntityManager
import javax.sql.DataSource

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest
@ContextConfiguration(classes = [MappingTestConfig.class])
@Import(TestConfig.class)
class MappingTest {

    @Autowired MockMvc mockMvc
    @MockBean GroovyShellConfig groovyShellConfig
    @MockBean GroovyShellRestController groovyShellRestController
    @MockBean AuthorRepository authorRepository
    @MockBean TeiFileRepository teiFileRepository
    @MockBean TeiDivRepository teiDivRepository
    @MockBean EntityManager entityManager
    @MockBean AdminService adminService
    @MockBean DataSource dataSource
    @MockBean TeiRepo teiRepo
    @MockBean RelocationRepository relocationRepository

    @Autowired
    ApplicationContext applicationContext

    @Test
    void test1() {

        // author page
        this.mockMvc
                .perform(get("/alecsandri"))
                .andExpect(MockMvcResultMatchers.handler().handlerType(DivController.class))
                .andExpect(MockMvcResultMatchers.handler().methodName("author"));

        // work TOC page
        this.mockMvc
                .perform(get("/alecsandri/scrieri"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.handler().handlerType(DivController.class))
                .andExpect(MockMvcResultMatchers.handler().methodName("teiDivAsHtml"));

        // chapter page
        this.mockMvc
                .perform(get("/alecsandri/scrieri/poem"))
                .andExpect(MockMvcResultMatchers.handler().handlerType(DivController.class))
                .andExpect(MockMvcResultMatchers.handler().methodName("teiDivAsHtml"));

        // binary object (not page)
        this.mockMvc
                .perform(get("/alecsandri/scrieri/_binary/12334"))
                .andExpect(MockMvcResultMatchers.handler().handlerType(DivController.class))
                .andExpect(MockMvcResultMatchers.handler().methodName("binaryObject"));

        this.mockMvc
                .perform(get("/alecsandri/scrieri/some_volume/_binary/12334"))
                .andExpect(MockMvcResultMatchers.handler().handlerType(DivController.class))
                .andExpect(MockMvcResultMatchers.handler().methodName("binaryObject"));

        this.mockMvc
                .perform(get("/alecsandri/scrieri/some_volume/some_poem/_binary/12334"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.handler().handlerType(DivController.class))
                .andExpect(MockMvcResultMatchers.handler().methodName("binaryObject"));

        this.mockMvc
                .perform(get("/alecsandri/scrieri/some_volume/some_subvolume/some_poem/_binary/abcdef"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.handler().handlerType(DivController.class))
                .andExpect(MockMvcResultMatchers.handler().methodName("binaryObject"));

    }

    def p(args) { println args }
}

@TestConfiguration
@Import(RunStuffOnStartup.class)
class MappingTestConfig {

    @Bean
    CommandLineRunner printJdbcUrlCLR(DataSource dataSource) {
        // NOOP
    }

}