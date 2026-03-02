-- 初始化随机种子
math.randomseed(os.time())

request = function()
   -- 1. 模拟用户坐标 (Wellington 核心区)
   local lat = -41.2865 + (math.random() - 0.5) * 0.2
   local lon = 174.7762 + (math.random() - 0.5) * 0.2

   -- 2. 模拟真实半径分布 (大部分人查 1-10km，少数人查 50km+)
   local radius
   local r_rand = math.random()
   if r_rand < 0.7 then
      radius = 1.0 + math.random() * 9.0  -- 70% 的人查 1-10km
   elseif r_rand < 0.9 then
      radius = 10.0 + math.random() * 40.0 -- 20% 的人查 10-50km
   else
      radius = 50.0 + math.random() * 150.0 -- 10% 的人查 50-200km
   end

   -- 3. 模拟翻页权重 (Zipf 定律：越往后翻页的人越少)
   local page = 0
   local p_rand = math.random()
   if p_rand < 0.6 then
      page = 0                             -- 60% 的人只看第 0 页
   elseif p_rand < 0.85 then
      page = math.random(1, 2)             -- 25% 的人翻 1-2 页
   else
      page = math.random(3, 10)            -- 15% 的人翻 3-10 页
   end

   -- 4. 随机分页大小 (5, 10, 20)
   local sizes = {5, 10, 20}
   local size = sizes[math.random(1, #sizes)]

   -- 5. 构建路径
   local path = string.format("/api/v1/persons/nearby?lat=%.4f&lon=%.4f&radius=%.1f&page=%d&size=%d",
                              lat, lon, radius, page, size)

   return wrk.format("GET", path)
end