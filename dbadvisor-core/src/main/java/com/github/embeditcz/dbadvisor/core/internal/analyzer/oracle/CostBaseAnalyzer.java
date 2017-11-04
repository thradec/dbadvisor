package com.github.embeditcz.dbadvisor.core.internal.analyzer.oracle;

import java.util.Map;

import com.github.embeditcz.dbadvisor.core.analyzer.ExecutionPlanContext;
import com.github.embeditcz.dbadvisor.core.internal.analyzer.AbstractExecutionPlanAnalyzer;
import com.github.embeditcz.dbadvisor.core.issue.Issue;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Component;

@Component
public class CostBaseAnalyzer extends AbstractExecutionPlanAnalyzer {

    private final ConversionService conversionService = new DefaultConversionService();
    private CostBaseProperties properties;

    public CostBaseAnalyzer(CostBaseProperties properties) {
        super(properties::isEnabled);
        this.properties = properties;
    }

    @Override
    protected String getName() {
        return "Cost";
    }

    @Override
    protected void analyzeImpl(ExecutionPlanContext ctx) {
        Map<String, Object> firstLine = ctx.getPlanData().get(0);
        long cost = conversionService.convert(firstLine.get("COST"), Long.class);
        if (cost >= properties.getThreshold()) {
            Issue issue = issueBuilder.builder()
                    .type(getName())
                    .query(ctx.getQueryContext())
                    .weight(cost)
                    .description("Costs for query are over the limit")
                    .metadata("elapsedTime", ctx.getQueryContext().getExecInfo().getElapsedTime())
                    .build();
            issueRepository.addIssue(issue);
        }
    }
}
