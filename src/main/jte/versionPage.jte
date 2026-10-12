@import tlm.dev.lectern.site.pages.ChangeLine
@import tlm.dev.lectern.site.pages.ChangeSection
@import tlm.dev.lectern.site.pages.DomainBlock
@import tlm.dev.lectern.site.pages.FactRow
@import tlm.dev.lectern.site.pages.SentencePart
@import tlm.dev.lectern.site.pages.VersionPageView
@param VersionPageView view
@template.layout.page(frame = view.frame(), content = @`
    <h1>${view.id()}@if(view.unstable()) <span class="unstable">unstable</span>@endif</h1>
    <dl class="sheet">
        @for(FactRow fact : view.facts())
            <dt>${fact.label()}</dt>
            <dd>@if(fact.hasLink())<a href="${fact.href()}">${fact.text()}</a>@else${fact.text()}@endif</dd>
        @endfor
    </dl>
    @if(view.sections().isEmpty())
        <p class="muted">${view.comparisonNote()}</p>
    @endif
    @for(ChangeSection section : view.sections())
        <section>
            <h2>Changes since @if(section.hasBaseLink())<a href="${section.baseHref()}">${section.baseId()}</a>@else${section.baseId()}@endif</h2>
            <p class="muted">${section.note()}</p>
            <p>${section.summary()}</p>
            @for(String warning : section.warnings())
                <p class="warning"><strong>Warning:</strong> ${warning}</p>
            @endfor
            @for(DomainBlock block : section.domains())
                <h3>${block.name()} <span class="count">${block.countText()}</span></h3>
                @if(!block.lines().isEmpty())
                    <ul class="changes">
                        @for(ChangeLine line : block.lines())
                            <li>@for(SentencePart part : line.parts())@if(part.value())<code>${part.text()}</code>@else${part.text()}@endif@endfor</li>
                        @endfor
                    </ul>
                @endif
                @if(block.hasShifts())
                    <p class="shifts">@for(SentencePart part : block.shifts())@if(part.value())<code>${part.text()}</code>@else${part.text()}@endif@endfor</p>
                @endif
            @endfor
        </section>
    @endfor
`)
