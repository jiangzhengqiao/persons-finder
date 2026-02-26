-- 初始化随机种子
math.randomseed(os.time())

request = function()
   -- 1. 模拟用户在 Wellington 附近随机位置 (调整范围以匹配你 DB 的数据分布)
   local lat = -41.2865 + (math.random() - 0.5) * 0.5
   local lon = 174.7762 + (math.random() - 0.5) * 0.5
   
   -- 2. 模拟用户随机翻页 (0 到 5 页)
   local page = math.random(0, 5)
   
   -- 3. 构建路径
   local path = string.format("/api/v1/persons/nearby?lat=%.4f&lon=%.4f&radius=100.0&page=%d&size=10", lat, lon, page)
   
   return wrk.format("GET", path)
end
