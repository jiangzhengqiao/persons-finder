-- 初始化城市列表和权重
local cities = {
    {name = "Sydney", lat = -33.8688, lon = 151.2093, weight = 545.0},
    {name = "Melbourne", lat = -37.8136, lon = 144.9631, weight = 535.0},
    {name = "Brisbane", lat = -27.4705, lon = 153.0260, weight = 271.0},
    {name = "Perth", lat = -31.9505, lon = 115.8605, weight = 214.0},
    {name = "Adelaide", lat = -34.9285, lon = 138.6007, weight = 139.0},
    {name = "Auckland", lat = -36.8485, lon = 174.7633, weight = 147.0},
    {name = "Christchurch", lat = -43.5321, lon = 172.6362, weight = 38.3},
    {name = "Wellington", lat = -41.2865, lon = 174.7762, weight = 21.6}
}

local total_weight = 0
for _, city in ipairs(cities) do
    total_weight = total_weight + city.weight
end

math.randomseed(os.time())

-- 根据权重选择城市
local function get_random_city()
    local r = math.random() * total_weight
    local current = 0
    for _, city in ipairs(cities) do
        current = current + city.weight
        if r <= current then return city end
    end
    return cities[1]
end

request = function()
   -- 1. 随机选一个核心城市
   local city = get_random_city()

   -- 2. 在城市中心周围随机偏移 (0.1 约 10km)
   -- 使用 Box-Muller 变换模拟高斯分布（可选，简单点就用 math.random）
   local lat = city.lat + (math.random() - 0.5) * 0.2
   local lon = city.lon + (math.random() - 0.5) * 0.2

   -- 3. 半径分布 (保持你之前的优秀逻辑)
   local radius
   local r_rand = math.random()
   if r_rand < 0.7 then
      radius = 1.0 + math.random() * 9.0
   elseif r_rand < 0.9 then
      radius = 10.0 + math.random() * 40.0
   else
      radius = 50.0 + math.random() * 150.0
   end

   -- 4. 翻页权重
   local page = 0
   local p_rand = math.random()
   if p_rand < 0.6 then
      page = 0
   elseif p_rand < 0.85 then
      page = math.random(1, 2)
   else
      page = math.random(3, 10)
   end

   local path = string.format("/api/v1/persons/nearby?lat=%.4f&lon=%.4f&radius=%.1f&page=%d&size=10",
                              lat, lon, radius, page)

   return wrk.format("GET", path)
end