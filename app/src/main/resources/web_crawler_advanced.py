import asyncio
import sys
from crawl4ai import *
from playwright.async_api import async_playwright

async def crawl_with_playwright_direct(url):
    """使用Playwright直接爬取，绕过crawl4ai的一些限制"""
    try:
        async with async_playwright() as p:
            # 启动浏览器
            browser = await p.chromium.launch(
                headless=True,
                args=[
                    '--no-sandbox',
                    '--disable-blink-features=AutomationControlled',
                    '--disable-web-security',
                    '--disable-features=VizDisplayCompositor'
                ]
            )
            
            # 创建上下文，模拟真实用户
            context = await browser.new_context(
                user_agent='Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
                viewport={'width': 1920, 'height': 1080},
                locale='zh-CN',
                timezone_id='Asia/Shanghai'
            )
            
            # 设置额外的请求头
            await context.set_extra_http_headers({
                'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8',
                'Accept-Language': 'zh-CN,zh;q=0.9,en;q=0.8',
                'Accept-Encoding': 'gzip, deflate, br',
                'Cache-Control': 'no-cache',
                'Pragma': 'no-cache',
                'Sec-Fetch-Dest': 'document',
                'Sec-Fetch-Mode': 'navigate',
                'Sec-Fetch-Site': 'none',
                'Sec-Fetch-User': '?1',
                'Upgrade-Insecure-Requests': '1'
            })
            
            page = await context.new_page()
            
            # 隐藏webdriver特征
            await page.add_init_script("""
                Object.defineProperty(navigator, 'webdriver', {
                    get: () => undefined,
                });
                
                window.chrome = {
                    runtime: {},
                };
                
                Object.defineProperty(navigator, 'plugins', {
                    get: () => [1, 2, 3, 4, 5],
                });
                
                Object.defineProperty(navigator, 'languages', {
                    get: () => ['zh-CN', 'zh', 'en'],
                });
            """)
            
            print(f"正在访问: {url}")
            
            # 访问页面
            response = await page.goto(url, wait_until='networkidle', timeout=30000)
            
            print(f"响应状态: {response.status}")
            
            # 等待页面完全加载
            await page.wait_for_timeout(3000)
            
            # 获取页面内容
            content = await page.content()
            print(f"页面HTML长度: {len(content)}")
            
            if len(content) > 1000:  # 如果获取到了实际内容
                # 使用BeautifulSoup解析
                from bs4 import BeautifulSoup
                soup = BeautifulSoup(content, 'html.parser')
                
                # 移除不需要的元素
                for element in soup(['script', 'style', 'nav', 'footer', 'aside', 'header']):
                    element.decompose()
                
                # 查找主要内容
                main_content = None
                
                # 尝试多种选择器
                selectors = [
                    '.content', '.main-content', '.article-content',
                    '.post-content', '.entry-content', 'article',
                    '.text-content', '.detail-content', '.news-content',
                    '[class*="content"]', '[id*="content"]',
                    '.main', '.container', '.wrapper'
                ]
                
                for selector in selectors:
                    elements = soup.select(selector)
                    if elements:
                        text_content = elements[0].get_text(strip=True)
                        print(f"选择器 {selector}: 找到 {len(elements)} 个元素，文本长度: {len(text_content)}")
                        if len(text_content) > 50:  # 降低要求
                            main_content = elements[0]
                            print(f"✅ 使用选择器: {selector}")
                            break
                
                if not main_content:
                    # 如果没找到特定区域，使用body
                    body = soup.find('body')
                    if body:
                        main_content = body
                        print("✅ 使用整个body作为内容区域")
                    else:
                        main_content = soup
                        print("✅ 使用整个文档作为内容区域")
                
                # 提取文本
                text = main_content.get_text(separator='\n', strip=True)
                
                # 清理文本
                lines = [line.strip() for line in text.split('\n') if line.strip()]
                clean_text = '\n'.join(lines)
                
                print(f"提取的文本长度: {len(clean_text)}")
                
                if len(clean_text) > 50:  # 降低要求
                    print("\n=== 提取的内容 ===")
                    print(clean_text[:3000])  # 显示前3000字符
                    return clean_text
                else:
                    print("提取的文本内容太少")
                    print(f"文本预览: {clean_text[:200]}")
                    return None
            else:
                print("页面内容太少，可能被反爬虫机制阻止")
                print(f"页面内容预览: {content[:200]}")
                return None
                
            await browser.close()
            
    except Exception as e:
        print(f"Playwright爬取出错: {str(e)}")
        import traceback
        traceback.print_exc()
        return None

async def main():
    if len(sys.argv) < 2:
        print("Error: URL parameter is required")
        sys.exit(1)
    
    url = sys.argv[1]
    
    print("=== 使用高级爬虫模式 ===")
    
    # 首先尝试Playwright直接爬取
    result = await crawl_with_playwright_direct(url)
    
    if result:
        print("\n✅ 爬取成功!")
        return
    
    print("\n❌ 高级爬取失败")
    print("可能的原因:")
    print("1. 网站有严格的反爬虫机制")
    print("2. 需要登录或特殊权限")
    print("3. 网站暂时不可访问")
    print("4. IP被限制")

if __name__ == "__main__":
    asyncio.run(main()) 