package com.resident.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WorkScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF5F7FA))
    ) {
        // 顶部横幅
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1B5E20), Color(0xFF2E7D32))
                    )
                )
                .padding(vertical = 32.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "网格化服务管理",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "润泽知园社区",
                    color = Color(0xFFB9F6CA),
                    fontSize = 15.sp,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 核心口号区域
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SloganCard(
                number = "01",
                slogan = "人在格中走，事在格中办",
                desc = "网格员深入每家每户，把服务送到居民家门口"
            )
            SloganCard(
                number = "02",
                slogan = "小格子里的大民生",
                desc = "聚焦居民急难愁盼，将问题解决在基层、消除在萌芽"
            )
            SloganCard(
                number = "03",
                slogan = "情况在网格中掌握",
                desc = "掌握辖区人员动态，做到底数清、情况明"
            )
            SloganCard(
                number = "04",
                slogan = "问题在网格中解决",
                desc = "第一时间发现问题，就地化解矛盾纠纷"
            )
            SloganCard(
                number = "05",
                slogan = "服务在网格中开展",
                desc = "把党的关怀和政府的温暖传递到每位居民"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 职责卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "网格员六项职责",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1B5E20)
                )
                Spacer(modifier = Modifier.height(12.dp))
                val duties = listOf(
                    "信息采集" to "采集辖区人员、房屋、单位等基础信息",
                    "巡查走访" to "定期巡查网格，走访慰问重点人群",
                    "隐患排查" to "排查安全隐患，及时上报处置",
                    "矛盾调解" to "化解邻里纠纷，维护社区和谐",
                    "宣传引导" to "宣传政策法规，引导居民依法办事",
                    "服务群众" to "帮助居民解决生活困难，传递党和政府关怀"
                )
                duties.forEach { (title, content) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .offset(y = 7.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF2E7D32))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                                color = Color(0xFF2E7D32))
                            Text(content, fontSize = 13.sp, color = Color(0xFF555555))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 底部标语
        Text(
            text = "用心服务每一位居民\n让社区更温暖、更和谐",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = Color(0xFF78909C),
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SloganCard(number: String, slogan: String, desc: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 编号圆圈
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = slogan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = desc,
                    fontSize = 13.sp,
                    color = Color(0xFF666666),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
