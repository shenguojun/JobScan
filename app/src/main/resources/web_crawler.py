import asyncio
import sys
from crawl4ai import *

async def main():
    if len(sys.argv) < 2:
        print("Error: URL parameter is required")
        sys.exit(1)
    
    url = sys.argv[1]
    
    async with AsyncWebCrawler() as crawler:
        config = CrawlerRunConfig(
            cache_mode=CacheMode.ENABLED,
            excluded_tags=['nav', 'footer', 'aside'],
            remove_overlay_elements=True,
            markdown_generator=DefaultMarkdownGenerator(
                content_filter=PruningContentFilter(threshold=0.48, threshold_type="fixed", min_word_threshold=0),
                options={
                    "ignore_links": True
                }
            ),
        )
        result = await crawler.arun(
            url=url,
            config=config,
        )
        
        # 尝试不同的输出方式
        if result.markdown and result.markdown.fit_markdown:
            print(result.markdown.fit_markdown)
        elif result.markdown and result.markdown.raw_markdown:
            print(result.markdown.raw_markdown)
        elif hasattr(result, 'cleaned_html') and result.cleaned_html:
            print(result.cleaned_html)
        elif hasattr(result, 'html') and result.html:
            # 如果没有markdown，至少输出HTML的前1000个字符
            print(result.html[:1000])
        else:
            print("No content found")

if __name__ == "__main__":
    asyncio.run(main()) 