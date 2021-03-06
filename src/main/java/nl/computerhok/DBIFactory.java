package nl.computerhok;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.codahale.metrics.jdbi.InstrumentedTimingCollector;
import com.codahale.metrics.jdbi.strategies.DelegatingStatementNameStrategy;
import com.codahale.metrics.jdbi.strategies.NameStrategies;
import com.codahale.metrics.jdbi.strategies.StatementNameStrategy;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.jdbi.*;
import io.dropwizard.jdbi.args.OptionalArgumentFactory;
import io.dropwizard.jdbi.logging.LogbackLog;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.ColonPrefixNamedParamStatementRewriter;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.StatementContext;
import org.slf4j.LoggerFactory;

import static com.codahale.metrics.MetricRegistry.name;

public class DBIFactory {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DBI.class);
    private static final String RAW_SQL = name(DBI.class, "raw-sql");

    private static class SanerNamingStrategy extends DelegatingStatementNameStrategy {
        private SanerNamingStrategy() {
            super(NameStrategies.CHECK_EMPTY,
                    NameStrategies.CONTEXT_CLASS,
                    NameStrategies.CONTEXT_NAME,
                    NameStrategies.SQL_OBJECT,
                    new StatementNameStrategy() {
                        @Override
                        public String getStatementName(StatementContext statementContext) {
                            return RAW_SQL;
                        }
                    });
        }
    }

    public DBI build(Environment environment,
                     DataSourceFactory configuration,
                     String name) {
        final ManagedDataSource dataSource = configuration.build(environment.metrics(), name);
        return build(environment, configuration, dataSource, name);
    }

    public DBI build(Environment environment,
                     DataSourceFactory dataSourceFactory,
                     ManagedDataSource dataSource,
                     String name) {
        final String validationQuery = dataSourceFactory.getValidationQuery();
        final DBI dbi = new DBI(dataSource);
        environment.lifecycle().manage(dataSource);
        environment.healthChecks().register(name, new DBIHealthCheck(dbi, validationQuery));
        dbi.setSQLLog(new LogbackLog(LOGGER, Level.TRACE));
        dbi.setTimingCollector(new InstrumentedTimingCollector(environment.metrics(), new SanerNamingStrategy()));
        if (dataSourceFactory.isAutoCommentsEnabled()) {
            dbi.setStatementRewriter(new NamePrependingStatementRewriter(new ColonPrefixNamedParamStatementRewriter()));
        }
        dbi.registerArgumentFactory(new OptionalArgumentFactory(dataSourceFactory.getDriverClass()));
        dbi.registerContainerFactory(new ImmutableListContainerFactory());
        dbi.registerContainerFactory(new ImmutableSetContainerFactory());
        dbi.registerContainerFactory(new OptionalContainerFactory());
        return dbi;
    }
}