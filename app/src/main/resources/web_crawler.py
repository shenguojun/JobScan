import asyncio
import sys
from crawl4ai import *
from playwright.async_api import async_playwright

async def crawl_with_playwright_fallback(url):
    """使用Playwright作为后备方案，处理复杂的中文网站"""
    try:
        async with async_playwright() as p:
            browser = await p.chromium.launch(
                headless=True,
                args=['--no-sandbox', '--disable-blink-features=AutomationControlled']
            )
            
            context = await browser.new_context(
                user_agent='Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
                viewport={'width': 1920, 'height': 1080},
                locale='zh-CN'
            )
            
            page = await context.new_page()
            
            # 隐藏webdriver特征
            await page.add_init_script("""
                Object.defineProperty(navigator, 'webdriver', {
                    get: () => undefined,
                });
            """)
            
            response = await page.goto(url, wait_until='networkidle', timeout=30000)
            await page.wait_for_timeout(3000)
            
            content = await page.content()
            await browser.close()
            
            if len(content) > 1000:
                from bs4 import BeautifulSoup
                soup = BeautifulSoup(content, 'html.parser')
                
                # 移除不需要的元素
                for element in soup(['script', 'style', 'nav', 'footer', 'aside', 'header']):
                    element.decompose()
                
                # 查找主要内容
                selectors = [
                    '.content', '.main-content', '.article-content',
                    '.post-content', '.entry-content', 'article',
                    '[class*="content"]', '[id*="content"]',
                    '.main', '.container', '.wrapper'
                ]
                
                main_content = None
                for selector in selectors:
                    elements = soup.select(selector)
                    if elements:
                        text_content = elements[0].get_text(strip=True)
                        if len(text_content) > 50:
                            main_content = elements[0]
                            break
                
                if not main_content:
                    main_content = soup.find('body') or soup
                
                text = main_content.get_text(separator='\n', strip=True)
                lines = [line.strip() for line in text.split('\n') if line.strip()]
                clean_text = '\n'.join(lines)
                
                if len(clean_text) > 50:
                    return clean_text
                    
            return None
            
    except Exception as e:
        return None

async def main():
    if len(sys.argv) < 2:
        print("Error: URL parameter is required")
        sys.exit(1)
    
    url = sys.argv[1]
    
    try:
        # 首先尝试crawl4ai
        async with AsyncWebCrawler(
            browser_type="chromium",
            headless=True,
            verbose=False
        ) as crawler:
            
            config = CrawlerRunConfig(
                cache_mode=CacheMode.ENABLED,
                wait_for="networkidle",
                delay_before_return_html=3.0,
                user_agent="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                excluded_tags=['script', 'style'],
                remove_overlay_elements=False,
                markdown_generator=DefaultMarkdownGenerator(
                    content_filter=PruningContentFilter(
                        threshold=0.05,
                        threshold_type="fixed", 
                        min_word_threshold=0
                    ),
                    options={
                        "ignore_links": False,
                        "unicode_snob": True,
                    }
                ),
            )
            
            result = await crawler.arun(url=url, config=config)
            
            # 尝试获取markdown内容
            if hasattr(result, 'markdown') and result.markdown:
                if hasattr(result.markdown, 'fit_markdown') and result.markdown.fit_markdown:
                    content = result.markdown.fit_markdown.strip()
                    if content and len(content) > 100:
                        print(content)
                        return
                
                if hasattr(result.markdown, 'raw_markdown') and result.markdown.raw_markdown:
                    content = result.markdown.raw_markdown.strip()
                    if content and len(content) > 100:
                        print(content)
                        return
            
            # 如果crawl4ai失败，使用Playwright后备方案
            fallback_result = await crawl_with_playwright_fallback(url)
            if fallback_result:
                print(fallback_result)
                return
            
            print("No content found")
            
    except Exception as e:
        # 如果出现异常，尝试Playwright后备方案
        fallback_result = await crawl_with_playwright_fallback(url)
        if fallback_result:
            print(fallback_result)
        else:
            print(f"Error: {str(e)}")
            sys.exit(1)

if __name__ == "__main__":
    asyncio.run(main()) 